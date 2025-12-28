package com.sky.dto;

import lombok.Data;

@Data
public class AdminUserStatusRequest {
    /**
     * 0 禁用 / 1 启用
     */
    private Integer status;
}

