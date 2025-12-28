package com.sky.service;

import com.sky.vo.CarouselItemVO;
import com.sky.vo.PortalPostDetailVO;
import com.sky.vo.PortalPostListItemVO;

import java.util.List;

public interface PortalService {

    List<CarouselItemVO> listCarousel();

    List<PortalPostListItemVO> listPosts(Integer type, Integer limit);

    PortalPostDetailVO getPostDetail(Long id);
}

