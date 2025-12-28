package com.sky.service;

import com.sky.dto.LoginRequest;
import com.sky.dto.RegisterRequest;
import com.sky.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);
}
