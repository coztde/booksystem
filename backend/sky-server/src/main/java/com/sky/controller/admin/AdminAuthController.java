package com.sky.controller.admin;

import com.sky.dto.AdminLoginRequest;
import com.sky.result.Result;
import com.sky.service.admin.AdminAuthService;
import com.sky.vo.AdminLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return Result.success(adminAuthService.login(request));
    }
}

