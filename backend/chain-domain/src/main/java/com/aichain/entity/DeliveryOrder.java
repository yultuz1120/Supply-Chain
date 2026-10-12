package com.aichain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrder implements Serializable {
    public static final int PENDING = 1;
    public static final int ACCEPTED = 2;
    public static final int DELIVERING = 3;
    public static final int DELIVERED = 4;
    public static final int CANCELLED = 5;

    private Long id;
    private String deliveryNumber;
    private Long storeId;
    private Long sourceOrderId;
    private Long courierId;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private Integer urgeCount;
    private LocalDateTime lastUrgeTime;
    private LocalDateTime createTime;
    private LocalDateTime acceptedTime;
    private LocalDateTime deliveredTime;
    private LocalDateTime updateTime;
}

