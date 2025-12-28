package com.sky.controller;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.BorrowedBookVO;
import com.sky.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public Result<UserProfileVO> me() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error(MessageConstant.USER_NOT_LOGIN);
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_LOGIN);
        }
        return Result.success(new UserProfileVO(user.getId(), user.getName(), user.getCode()));
    }

    @GetMapping("/borrowed")
    public Result<List<BorrowedBookVO>> borrowed() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error(MessageConstant.USER_NOT_LOGIN);
        }
        return Result.success(userService.listBorrowedByUserId(userId));
    }
}

