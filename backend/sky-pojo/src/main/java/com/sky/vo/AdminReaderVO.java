package com.sky.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReaderVO {
    private Long id;
    private String name;
    private String code;
    private String phone;
    private Long readerTypeId;
    private String readerTypeName;
    private Integer status;
    private LocalDateTime createTime;
}

