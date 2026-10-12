package com.aichain.mapper;

import com.github.pagehelper.Page;
import com.aichain.dto.GoodsSalesDTO;
import com.aichain.dto.OrdersPageQueryDTO;
import com.aichain.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Yultuz
 */
@Mapper
public interface OrderMapper{
    public void insert(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
    /**
     * 根据状态统计订单数量
     * @paramstatus
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer toBeConfirmed);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    void update(Orders orders);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    Double sumByMap(Map map);

    Integer countByMap(Map map);

    List<GoodsSalesDTO> getSalesTop(LocalDateTime begin, LocalDateTime end);
}
