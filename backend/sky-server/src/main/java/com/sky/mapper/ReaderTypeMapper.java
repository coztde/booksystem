package com.sky.mapper;

import com.sky.entity.ReaderType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReaderTypeMapper {

    List<ReaderType> listEnabled();

    ReaderType getById(@Param("id") Long id);
}

