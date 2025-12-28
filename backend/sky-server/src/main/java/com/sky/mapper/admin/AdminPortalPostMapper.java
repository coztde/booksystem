package com.sky.mapper.admin;

import com.sky.entity.PortalPost;
import com.sky.vo.AdminPortalPostListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminPortalPostMapper {

    long count(@Param("type") Integer type, @Param("status") Integer status, @Param("keyword") String keyword);

    List<AdminPortalPostListItemVO> list(@Param("type") Integer type,
                                        @Param("status") Integer status,
                                        @Param("keyword") String keyword,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);

    PortalPost getById(@Param("id") Long id);

    int insert(PortalPost post);

    int update(PortalPost post);

    int softDelete(@Param("id") Long id, @Param("updateUser") Long updateUser);
}

