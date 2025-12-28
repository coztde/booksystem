package com.sky.controller.admin;

import com.sky.dto.AdminPortalPostSaveRequest;
import com.sky.entity.PortalPost;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.AdminPortalPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/portal")
public class AdminPortalPostController {

    @Autowired
    private AdminPortalPostService adminPortalPostService;

    @GetMapping("/posts")
    public Result<PageResult> page(@RequestParam(required = false) Integer type,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminPortalPostService.page(type, status, keyword, page, pageSize));
    }

    @GetMapping("/posts/{id}")
    public Result<PortalPost> get(@PathVariable Long id) {
        return Result.success(adminPortalPostService.getById(id));
    }

    @PostMapping("/posts")
    public Result<Void> create(@RequestBody AdminPortalPostSaveRequest request) {
        adminPortalPostService.create(request);
        return Result.success();
    }

    @PutMapping("/posts/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AdminPortalPostSaveRequest request) {
        adminPortalPostService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminPortalPostService.delete(id);
        return Result.success();
    }
}

