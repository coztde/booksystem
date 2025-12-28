package com.sky.controller;

import com.sky.result.Result;
import com.sky.service.PortalService;
import com.sky.vo.CarouselItemVO;
import com.sky.vo.PortalPostDetailVO;
import com.sky.vo.PortalPostListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    @Autowired
    private PortalService portalService;

    @GetMapping("/carousel")
    public Result<List<CarouselItemVO>> carousel() {
        return Result.success(portalService.listCarousel());
    }

    @GetMapping("/posts")
    public Result<List<PortalPostListItemVO>> posts(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(portalService.listPosts(type, limit));
    }

    @GetMapping("/posts/{id}")
    public Result<PortalPostDetailVO> postDetail(@PathVariable Long id) {
        return Result.success(portalService.getPostDetail(id));
    }
}

