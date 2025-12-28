package com.sky.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalPostDetailVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private String coverUrl;
    private LocalDateTime publishTime;
}

