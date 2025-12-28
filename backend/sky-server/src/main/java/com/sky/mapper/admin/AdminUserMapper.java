package com.sky.mapper.admin;

import com.sky.entity.User;
import com.sky.vo.AdminReaderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserMapper {

    User getAdminByUsername(@Param("username") String username);

    long countReaders(@Param("keyword") String keyword, @Param("status") Integer status);

    List<AdminReaderVO> listReaders(@Param("keyword") String keyword,
                                   @Param("status") Integer status,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    int updatePasswordHash(@Param("id") Long id,
                           @Param("passwordHash") String passwordHash,
                           @Param("updateUser") Long updateUser);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateUser") Long updateUser);
}

