package com.aichain.vo;

import com.aichain.entity.DeliveryOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryOrderVO extends DeliveryOrder {
    private String storeName;
    private String courierName;
    private String courierPhone;
}

