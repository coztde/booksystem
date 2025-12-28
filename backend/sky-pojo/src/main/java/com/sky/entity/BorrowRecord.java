package com.sky.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BorrowRecord {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime borrowAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnAt;
    private Integer renewCount;
    private Integer status;
    private BigDecimal fineAmount;
    private Long handledBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private Integer isDeleted;
}

