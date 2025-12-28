package com.sky.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowedBookVO {
    private Long recordId;
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String location;
    private LocalDateTime borrowAt;
    private LocalDateTime dueAt;
    private Integer renewCount;
    private Integer status;
}

