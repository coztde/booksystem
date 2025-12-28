package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User getReaderByCode(@Param("code") String code);

    User getById(@Param("id") Long id);

    User getByCode(@Param("code") String code);

    User getByPhone(@Param("phone") String phone);

    int insertReader(User user);
}
