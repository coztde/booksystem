package com.sky.service.admin;

import com.sky.dto.AdminLoginRequest;
import com.sky.vo.AdminLoginResponse;

public interface AdminAuthService {
    AdminLoginResponse login(AdminLoginRequest request);
}

