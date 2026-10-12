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
public class Store implements Serializable {
    private Long id;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

