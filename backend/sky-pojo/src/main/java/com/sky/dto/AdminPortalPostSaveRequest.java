package com.sky.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPortalPostSaveRequest {
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
}

