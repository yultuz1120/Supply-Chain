package com.aichain.service;

import com.aichain.dto.CourierDTO;
import com.aichain.dto.DeliveryOrderPageQueryDTO;
import com.aichain.entity.Courier;
import com.aichain.result.PageResult;
import com.aichain.vo.DeliveryOrderVO;
import com.aichain.vo.DeliverySummaryVO;

import java.util.List;

public interface DeliveryService {
    PageResult page(DeliveryOrderPageQueryDTO query);
    DeliveryOrderVO getById(Long id);
    DeliverySummaryVO summary();
    List<Courier> couriers();
    void saveCourier(CourierDTO dto);
    void updateCourier(CourierDTO dto);
    void updateCourierStatus(Long id, Integer status);
    void assign(Long orderId, Long courierId);
    void updateStatus(Long orderId, Integer status);
    String urge(Long orderId, String source, String remark);
}
