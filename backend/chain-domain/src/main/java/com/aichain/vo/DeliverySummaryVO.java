package com.aichain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySummaryVO {
    private Integer storeCount;
    private Integer staffCount;
    private Integer courierCount;
    private Integer pendingCount;
    private Integer acceptedCount;
    private Integer deliveringCount;
    private Integer deliveredCount;
    private Integer urgedCount;
}
