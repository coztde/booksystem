package com.sky.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminBorrowReturnRequest {
    /**
     * 借阅记录ID
     */
    private Long recordId;
    /**
     * 罚款金额（可选）
     */
    private BigDecimal fineAmount;
}

