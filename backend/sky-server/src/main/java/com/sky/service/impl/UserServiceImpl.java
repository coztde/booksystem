package com.sky.service.impl;

import com.sky.entity.User;
import com.sky.mapper.BorrowRecordMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import com.sky.vo.BorrowedBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    public User getById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.getById(id);
    }

    @Override
    public List<BorrowedBookVO> listBorrowedByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return borrowRecordMapper.listBorrowedByUserId(userId);
    }
}
