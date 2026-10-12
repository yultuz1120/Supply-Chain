package com.aichain.dto;

import lombok.Data;

@Data
public class DeliveryOrderPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Long storeId;
    private Long courierId;
    private Integer status;
    private String deliveryNumber;
}

