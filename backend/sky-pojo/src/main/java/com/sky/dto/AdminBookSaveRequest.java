package com.sky.dto;

import lombok.Data;

@Data
public class AdminBookSaveRequest {
    private String coverUrl;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String category;
    private String location;
    private String description;
    private Integer totalQty;
    private Integer status;
}
