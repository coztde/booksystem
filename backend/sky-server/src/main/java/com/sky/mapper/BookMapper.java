package com.sky.mapper;

import com.sky.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {

    List<Book> listBooks(@Param("q") String q, @Param("category") String category);

    List<String> listCategories();

    Book getById(@Param("id") Long id);

    int decrementAvailableQty(@Param("id") Long id);

    int incrementAvailableQty(@Param("id") Long id);
}
