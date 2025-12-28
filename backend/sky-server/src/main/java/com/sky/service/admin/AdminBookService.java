package com.sky.service.admin;

import com.sky.dto.AdminBookSaveRequest;
import com.sky.entity.Book;
import com.sky.result.PageResult;

public interface AdminBookService {

    PageResult page(String keyword, String category, Integer status, Integer page, Integer pageSize);

    Book getById(Long id);

    void create(AdminBookSaveRequest request);

    void update(Long id, AdminBookSaveRequest request);

    void delete(Long id);
}

