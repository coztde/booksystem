package com.sky.service.impl;

import com.sky.entity.Book;
import com.sky.mapper.BookMapper;
import com.sky.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    public List<Book> listBooks(String q, String category) {
        String keyword = q == null ? null : q.trim();
        String cat = category == null ? null : category.trim();
        return bookMapper.listBooks(keyword, cat);
    }

    @Override
    public List<String> listCategories() {
        return bookMapper.listCategories();
    }

    @Override
    public Book getById(Long id) {
        if (id == null) {
            return null;
        }
        return bookMapper.getById(id);
    }
}
