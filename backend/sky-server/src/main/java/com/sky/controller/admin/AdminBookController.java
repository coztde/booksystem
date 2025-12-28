package com.sky.controller.admin;

import com.sky.dto.AdminBookSaveRequest;
import com.sky.entity.Book;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.AdminBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/books")
public class AdminBookController {

    @Autowired
    private AdminBookService adminBookService;

    @GetMapping
    public Result<PageResult> page(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminBookService.page(keyword, category, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Book> get(@PathVariable Long id) {
        return Result.success(adminBookService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody AdminBookSaveRequest request) {
        adminBookService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AdminBookSaveRequest request) {
        adminBookService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminBookService.delete(id);
        return Result.success();
    }
}

