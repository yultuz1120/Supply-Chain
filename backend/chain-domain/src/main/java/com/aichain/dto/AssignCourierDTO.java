package com.aichain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AssignCourierDTO {
    @NotNull
    private Long courierId;
}

