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
public class UrgeRecord implements Serializable {
    private Long id;
    private Long deliveryOrderId;
    private Long storeId;
    private Long operatorId;
    private String source;
    private String remark;
    private LocalDateTime createTime;
}

