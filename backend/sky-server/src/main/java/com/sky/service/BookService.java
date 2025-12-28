package com.sky.service;

import com.sky.entity.Book;

import java.util.List;

public interface BookService {

    List<Book> listBooks(String q, String category);

    List<String> listCategories();

    Book getById(Long id);
}
