package com.sky.controller.admin;

import com.sky.dto.AdminBorrowCreateRequest;
import com.sky.dto.AdminBorrowReturnRequest;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.AdminBorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/borrows")
public class AdminBorrowController {

    @Autowired
    private AdminBorrowService adminBorrowService;

    @GetMapping
    public Result<PageResult> page(@RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminBorrowService.page(status, keyword, page, pageSize));
    }

    @PostMapping("/borrow")
    public Result<Void> borrow(@RequestBody AdminBorrowCreateRequest request) {
        adminBorrowService.borrow(request);
        return Result.success();
    }

    @PostMapping("/return")
    public Result<Void> returnBook(@RequestBody AdminBorrowReturnRequest request) {
        adminBorrowService.returnBook(request);
        return Result.success();
    }
}

