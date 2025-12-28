package com.sky.controller;

import com.sky.entity.Book;
import com.sky.result.Result;
import com.sky.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public Result<List<Book>> listBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
        return Result.success(bookService.listBooks(q, category));
    }

    @GetMapping("/categories")
    public Result<List<String>> listCategories() {
        return Result.success(bookService.listCategories());
    }

    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }
}
