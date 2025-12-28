package com.sky.mapper.admin;

import com.sky.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminBookMapper {

    long count(@Param("keyword") String keyword, @Param("category") String category, @Param("status") Integer status);

    List<Book> list(@Param("keyword") String keyword,
                    @Param("category") String category,
                    @Param("status") Integer status,
                    @Param("offset") Integer offset,
                    @Param("pageSize") Integer pageSize);

    Book getById(@Param("id") Long id);

    int insert(Book book);

    int update(Book book);

    int softDelete(@Param("id") Long id, @Param("updateUser") Long updateUser);
}

