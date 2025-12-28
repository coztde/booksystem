package com.sky.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReaderType {
    private Long id;
    private String name;
    private Integer maxBorrow;
    private Integer borrowDays;
    private Integer maxRenew;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private Integer isDeleted;
}

