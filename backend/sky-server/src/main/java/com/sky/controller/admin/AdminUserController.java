package com.sky.controller.admin;

import com.sky.dto.AdminUserStatusRequest;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult> page(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminUserService.pageReaders(keyword, status, page, pageSize));
    }

    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return Result.success();
    }

    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody AdminUserStatusRequest request) {
        adminUserService.updateStatus(id, request == null ? null : request.getStatus());
        return Result.success();
    }
}

