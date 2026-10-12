package com.aichain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CourierDTO {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    private Integer onlineStatus = 1;
}

