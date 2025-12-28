package com.sky.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPortalPostListItemVO {
    private Long id;
    private Integer type;
    private String tag;
    private String title;
    private String subtitle;
    private String coverUrl;
    private String accent;
    private Integer sort;
    private LocalDateTime publishTime;
    private Integer status;
}

