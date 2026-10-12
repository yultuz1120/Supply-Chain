package com.aichain.service;

import com.aichain.dto.OrdersConfirmDTO;
import com.aichain.dto.OrdersPageQueryDTO;
import com.aichain.dto.OrdersSubmitDTO;
import com.aichain.result.PageResult;
import com.aichain.vo.OrderStatisticsVO;
import com.aichain.vo.OrderSubmitVO;

/**
 * @author Yultuz
 */
public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    PageResult pageQuery4User(int page, int pageSize, Integer status);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();


    void reminder(Long id);
}
