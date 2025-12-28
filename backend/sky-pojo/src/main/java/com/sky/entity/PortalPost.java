package com.sky.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalPost {
    private Long id;
    private Integer type;
    private String tag;
    private String title;
    private String subtitle;
    private String content;
    private String coverUrl;
    private String accent;
    private Integer sort;
    private LocalDateTime publishTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private Integer isDeleted;
}

