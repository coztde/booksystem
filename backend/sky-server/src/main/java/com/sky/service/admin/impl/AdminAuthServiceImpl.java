package com.sky.service.admin.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.AdminLoginRequest;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.admin.AdminUserMapper;
import com.sky.properties.JwtProperties;
import com.sky.service.admin.AdminAuthService;
import com.sky.utils.JwtUtil;
import com.sky.utils.PasswordUtil;
import com.sky.vo.AdminLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        if (request == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String password = request.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }

        User admin = adminUserMapper.getAdminByUsername(username);
        if (admin == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!PasswordUtil.matchesSha256(password, admin.getPasswordHash())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, admin.getId());
        claims.put(JwtClaimsConstant.NAME, admin.getName());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        return new AdminLoginResponse(token, admin.getId(), admin.getName(), admin.getUsername());
    }
}

