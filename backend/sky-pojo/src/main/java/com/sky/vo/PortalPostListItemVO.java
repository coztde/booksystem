package com.sky.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalPostListItemVO {
    private Long id;
    private Integer type;
    private String title;
    private String subtitle;
    private LocalDateTime publishTime;
}

