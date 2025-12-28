package com.sky.service.admin;

import com.sky.result.PageResult;

public interface AdminUserService {

    PageResult pageReaders(String keyword, Integer status, Integer page, Integer pageSize);

    void resetPassword(Long id);

    void updateStatus(Long id, Integer status);
}

