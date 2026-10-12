package com.aichain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreOverviewVO {
    private Long id;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
    private Integer pendingCount;
    private Integer deliveringCount;
    private Integer deliveredCount;
    private Integer urgedCount;
}

