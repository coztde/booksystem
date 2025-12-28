package com.sky.dto;

import lombok.Data;

@Data
public class AdminBorrowCreateRequest {
    /**
     * 读者学号/工号
     */
    private String userCode;
    /**
     * 图书ID
     */
    private Long bookId;
}

