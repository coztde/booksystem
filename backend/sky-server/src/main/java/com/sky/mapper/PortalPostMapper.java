package com.sky.mapper;

import com.sky.vo.CarouselItemVO;
import com.sky.vo.PortalPostDetailVO;
import com.sky.vo.PortalPostListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PortalPostMapper {

    List<CarouselItemVO> listCarousel();

    List<PortalPostListItemVO> listPosts(@Param("type") Integer type, @Param("limit") Integer limit);

    PortalPostDetailVO getDetailById(@Param("id") Long id);
}

