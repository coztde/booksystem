package com.sky.controller;

import com.sky.dto.BorrowRequest;
import com.sky.dto.RenewRequest;
import com.sky.dto.ReturnRequest;
import com.sky.result.Result;
import com.sky.service.BorrowService;
import com.sky.vo.BorrowedBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/borrow")
    public Result<Void> borrow(@RequestBody BorrowRequest request) {
        borrowService.borrow(request == null ? null : request.getBookId());
        return Result.success();
    }

    @PostMapping("/return")
    public Result<Void> returnBook(@RequestBody ReturnRequest request) {
        borrowService.returnBook(request == null ? null : request.getRecordId());
        return Result.success();
    }

    @PostMapping("/renew")
    public Result<Void> renew(@RequestBody RenewRequest request) {
        borrowService.renew(request == null ? null : request.getRecordId());
        return Result.success();
    }

    @GetMapping("/current")
    public Result<List<BorrowedBookVO>> current() {
        return Result.success(borrowService.listCurrent());
    }
}

