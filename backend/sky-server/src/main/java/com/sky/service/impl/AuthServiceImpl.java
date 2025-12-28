package com.sky.service.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.LoginRequest;
import com.sky.dto.RegisterRequest;
import com.sky.entity.ReaderType;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.ReaderTypeMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.service.AuthService;
import com.sky.utils.JwtUtil;
import com.sky.utils.PasswordUtil;
import com.sky.vo.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReaderTypeMapper readerTypeMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getReaderByCode(request.getCode().trim());
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!PasswordUtil.matchesSha256(request.getPassword(), user.getPasswordHash())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        claims.put(JwtClaimsConstant.CODE, user.getCode());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return new LoginResponse(token, user.getId(), user.getName(), user.getCode());
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        if (request == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        String name = request.getName() == null ? null : request.getName().trim();
        String code = request.getCode() == null ? null : request.getCode().trim();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        String password = request.getPassword();

        if (name == null || name.isEmpty() || code == null || code.isEmpty() || phone == null || phone.isEmpty()) {
            throw new BaseException("注册信息不完整");
        }
        if (password == null || password.length() < 6) {
            throw new BaseException("密码长度至少6位");
        }

        if (userMapper.getByCode(code) != null) {
            throw new BaseException("学号/工号已存在");
        }
        if (userMapper.getByPhone(phone) != null) {
            throw new BaseException("手机号已存在");
        }

        Long readerTypeId = request.getReaderTypeId() == null ? 1L : request.getReaderTypeId();
        ReaderType readerType = readerTypeMapper.getById(readerTypeId);
        if (readerType == null) {
            throw new BaseException("读者类型不存在");
        }

        User user = new User();
        user.setName(name);
        user.setCode(code);
        user.setPhone(phone);
        user.setReaderTypeId(readerTypeId);
        user.setPasswordHash(PasswordUtil.sha256Hex(password));

        int inserted = userMapper.insertReader(user);
        if (inserted != 1 || user.getId() == null) {
            throw new BaseException("注册失败");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        claims.put(JwtClaimsConstant.CODE, user.getCode());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        return new LoginResponse(token, user.getId(), user.getName(), user.getCode());
    }
}
