package com.sky.service.admin;

import com.sky.dto.AdminBorrowCreateRequest;
import com.sky.dto.AdminBorrowReturnRequest;
import com.sky.result.PageResult;

public interface AdminBorrowService {

    PageResult page(Integer status, String keyword, Integer page, Integer pageSize);

    void borrow(AdminBorrowCreateRequest request);

    void returnBook(AdminBorrowReturnRequest request);
}

