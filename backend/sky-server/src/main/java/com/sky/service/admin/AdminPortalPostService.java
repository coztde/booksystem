package com.sky.service.admin;

import com.sky.dto.AdminPortalPostSaveRequest;
import com.sky.entity.PortalPost;
import com.sky.result.PageResult;

public interface AdminPortalPostService {

    PageResult page(Integer type, Integer status, String keyword, Integer page, Integer pageSize);

    PortalPost getById(Long id);

    void create(AdminPortalPostSaveRequest request);

    void update(Long id, AdminPortalPostSaveRequest request);

    void delete(Long id);
}

