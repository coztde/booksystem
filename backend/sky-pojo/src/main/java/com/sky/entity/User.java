package com.sky.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private Integer role;
    private String username;
    private String phone;
    private String passwordHash;
    private String name;
    private String code;
    private Long readerTypeId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private Integer isDeleted;
}

