package com.aichain.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.aichain.context.BaseContext;
import com.aichain.dto.CourierDTO;
import com.aichain.dto.DeliveryOrderPageQueryDTO;
import com.aichain.entity.Courier;
import com.aichain.entity.DeliveryOrder;
import com.aichain.entity.UrgeRecord;
import com.aichain.exception.BaseException;
import com.aichain.mapper.*;
import com.aichain.result.PageResult;
import com.aichain.service.DeliveryService;
import com.aichain.vo.DeliveryOrderVO;
import com.aichain.vo.DeliverySummaryVO;
import com.aichain.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final CourierMapper courierMapper;
    private final StoreMapper storeMapper;
    private final EmployeeMapper employeeMapper;
    private final UrgeRecordMapper urgeRecordMapper;
    private final WebSocketServer webSocketServer;

    public DeliveryServiceImpl(DeliveryOrderMapper deliveryOrderMapper,
                               CourierMapper courierMapper,
                               StoreMapper storeMapper,
                               EmployeeMapper employeeMapper,
                               UrgeRecordMapper urgeRecordMapper,
                               WebSocketServer webSocketServer) {
        this.deliveryOrderMapper = deliveryOrderMapper;
        this.courierMapper = courierMapper;
        this.storeMapper = storeMapper;
        this.employeeMapper = employeeMapper;
        this.urgeRecordMapper = urgeRecordMapper;
        this.webSocketServer = webSocketServer;
    }

    @Override
    public PageResult page(DeliveryOrderPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<DeliveryOrderVO> result = deliveryOrderMapper.page(query);
        return new PageResult(result.getTotal(), result.getResult());
    }

    @Override
    public DeliveryOrderVO getById(Long id) {
        DeliveryOrderVO order = deliveryOrderMapper.getById(id);
        if (order == null) {
            throw new BaseException("配送订单不存在");
        }
        return order;
    }

    @Override
    public DeliverySummaryVO summary() {
        return DeliverySummaryVO.builder()
                .storeCount(storeMapper.countAll())
                .staffCount(employeeMapper.countAll())
                .courierCount(courierMapper.countAll())
                .pendingCount(deliveryOrderMapper.countByStatus(DeliveryOrder.PENDING))
                .acceptedCount(deliveryOrderMapper.countByStatus(DeliveryOrder.ACCEPTED))
                .deliveringCount(deliveryOrderMapper.countByStatus(DeliveryOrder.DELIVERING))
                .deliveredCount(deliveryOrderMapper.countByStatus(DeliveryOrder.DELIVERED))
                .urgedCount(deliveryOrderMapper.countUrged())
                .build();
    }

    @Override
    public List<Courier> couriers() {
        return courierMapper.list();
    }

    @Override
    public void saveCourier(CourierDTO dto) {
        Courier courier = new Courier();
        BeanUtils.copyProperties(dto, courier);
        courierMapper.insert(courier);
    }

    @Override
    public void updateCourier(CourierDTO dto) {
        if (dto.getId() == null || courierMapper.getById(dto.getId()) == null) {
            throw new BaseException("配送员不存在");
        }
        Courier courier = new Courier();
        BeanUtils.copyProperties(dto, courier);
        courierMapper.update(courier);
    }

    @Override
    public void updateCourierStatus(Long id, Integer status) {
        if (courierMapper.getById(id) == null) {
            throw new BaseException("配送员不存在");
        }
        courierMapper.updateStatus(id, status);
    }

    @Override
    public void assign(Long orderId, Long courierId) {
        DeliveryOrderVO order = getById(orderId);
        Courier courier = courierMapper.getById(courierId);
        if (courier == null || courier.getOnlineStatus() == null || courier.getOnlineStatus() != 1) {
            throw new BaseException("请选择在线配送员");
        }
        if (order.getStatus() != DeliveryOrder.PENDING) {
            throw new BaseException("只有待接单订单可以指派配送员");
        }
        deliveryOrderMapper.assign(orderId, courierId);
    }

    @Override
    public void updateStatus(Long orderId, Integer status) {
        DeliveryOrderVO order = getById(orderId);
        if (status < DeliveryOrder.PENDING || status > DeliveryOrder.CANCELLED) {
            throw new BaseException("配送状态不合法");
        }
        if (status == DeliveryOrder.DELIVERING && order.getCourierId() == null) {
            throw new BaseException("请先指派配送员");
        }
        deliveryOrderMapper.updateStatus(orderId, status);
    }

    @Override
    @Transactional
    public String urge(Long orderId, String source, String remark) {
        DeliveryOrderVO order = getById(orderId);
        if (order.getStatus() >= DeliveryOrder.DELIVERED) {
            throw new BaseException("该配送单已结束，不能催单");
        }
        LocalDateTime now = LocalDateTime.now();
        if (deliveryOrderMapper.incrementUrge(orderId, now) == 0) {
            throw new BaseException("催单失败，请刷新后重试");
        }
        UrgeRecord record = UrgeRecord.builder()
                .deliveryOrderId(orderId)
                .storeId(order.getStoreId())
                .operatorId(BaseContext.getCurrentId())
                .source(source == null ? "PAGE" : source)
                .remark(remark)
                .createTime(now)
                .build();
        urgeRecordMapper.insert(record);

        Map<String, Object> message = new HashMap<>();
        message.put("type", 2);
        message.put("deliveryOrderId", orderId);
        message.put("content", "门店 " + order.getStoreId() + " 催促配送单 " + order.getDeliveryNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(message));
        return "催单成功，配送单 " + order.getDeliveryNumber() + " 已通知配送中心";
    }
}

