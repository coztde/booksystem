package com.sky.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    /**
     * 学号/工号
     */
    private String code;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 明文密码（服务端会做hash存储）
     */
    private String password;
    /**
     * 读者类型ID（可空，默认本科生）
     */
    private Long readerTypeId;
}

