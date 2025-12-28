package com.sky.service.impl;

import com.sky.exception.BaseException;
import com.sky.mapper.PortalPostMapper;
import com.sky.service.PortalService;
import com.sky.vo.CarouselItemVO;
import com.sky.vo.PortalPostDetailVO;
import com.sky.vo.PortalPostListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalServiceImpl implements PortalService {

    @Autowired
    private PortalPostMapper portalPostMapper;

    @Override
    public List<CarouselItemVO> listCarousel() {
        return portalPostMapper.listCarousel();
    }

    @Override
    public List<PortalPostListItemVO> listPosts(Integer type, Integer limit) {
        int resolvedLimit = limit == null ? 10 : Math.min(Math.max(limit, 1), 100);
        return portalPostMapper.listPosts(type, resolvedLimit);
    }

    @Override
    public PortalPostDetailVO getPostDetail(Long id) {
        if (id == null) {
            throw new BaseException("内容不存在");
        }
        PortalPostDetailVO vo = portalPostMapper.getDetailById(id);
        if (vo == null) {
            throw new BaseException("内容不存在");
        }
        return vo;
    }
}

