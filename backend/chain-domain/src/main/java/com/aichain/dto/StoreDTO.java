package com.aichain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class StoreDTO {
    private Long id;
    @NotBlank
    private String name;
    private Integer status = 1;
}

