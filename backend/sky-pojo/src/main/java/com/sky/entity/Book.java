package com.sky.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Book {
    private Long id;
    private String coverUrl;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String category;
    private String location;
    private String description;
    private Integer totalQty;
    private Integer availableQty;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private Integer isDeleted;
}
