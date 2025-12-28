package com.sky.service;

import com.sky.entity.User;
import com.sky.vo.BorrowedBookVO;

import java.util.List;

public interface UserService {

    User getById(Long id);

    List<BorrowedBookVO> listBorrowedByUserId(Long userId);
}

