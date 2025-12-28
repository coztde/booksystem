package com.sky.dto;

import lombok.Data;

@Data
public class LoginRequest {
    /**
     * 学号/工号
     */
    private String code;
    /**
     * 明文密码（服务端会做hash比对）
     */
    private String password;
}

