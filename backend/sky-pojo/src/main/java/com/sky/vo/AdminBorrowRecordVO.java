package com.sky.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminBorrowRecordVO {
    private Long recordId;
    private Long userId;
    private String userName;
    private String userCode;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime borrowAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnAt;
    private Integer renewCount;
    /**
     * 0 借阅中 / 1 已还 / 2 逾期
     */
    private Integer status;
    private BigDecimal fineAmount;
    private Long handledBy;
    private String handledByName;
}

