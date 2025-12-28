package com.sky.service.admin.impl;

import com.sky.context.AdminContext;
import com.sky.dto.AdminPortalPostSaveRequest;
import com.sky.entity.PortalPost;
import com.sky.exception.BaseException;
import com.sky.mapper.admin.AdminPortalPostMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.AdminPortalPostService;
import com.sky.vo.AdminPortalPostListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AdminPortalPostServiceImpl implements AdminPortalPostService {

    @Autowired
    private AdminPortalPostMapper adminPortalPostMapper;

    @Override
    public PageResult page(Integer type, Integer status, String keyword, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (p - 1) * ps;

        long total = adminPortalPostMapper.count(type, status, keyword);
        List<AdminPortalPostListItemVO> records =
                total == 0 ? Collections.emptyList() : adminPortalPostMapper.list(type, status, keyword, offset, ps);
        return new PageResult(total, records);
    }

    @Override
    public PortalPost getById(Long id) {
        if (id == null) {
            throw new BaseException("缺少ID");
        }
        PortalPost post = adminPortalPostMapper.getById(id);
        if (post == null) {
            throw new BaseException("内容不存在");
        }
        return post;
    }

    @Override
    public void create(AdminPortalPostSaveRequest request) {
        if (request == null) {
            throw new BaseException("参数错误");
        }
        if (request.getType() == null || request.getType() < 1 || request.getType() > 3) {
            throw new BaseException("类型不合法");
        }
        String title = request.getTitle() == null ? null : request.getTitle().trim();
        if (title == null || title.isEmpty()) {
            throw new BaseException("标题不能为空");
        }

        Long adminId = AdminContext.getCurrentId();
        PortalPost post = new PortalPost();
        post.setType(request.getType());
        post.setTag(trimOrNull(request.getTag()));
        post.setTitle(title);
        post.setSubtitle(trimOrNull(request.getSubtitle()));
        post.setContent(request.getContent());
        post.setCoverUrl(trimOrNull(request.getCoverUrl()));
        post.setAccent(trimOrNull(request.getAccent()));
        post.setSort(request.getSort() == null ? 0 : request.getSort());
        post.setPublishTime(request.getPublishTime());
        post.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        post.setCreateUser(adminId);
        post.setUpdateUser(adminId);

        int inserted = adminPortalPostMapper.insert(post);
        if (inserted != 1) {
            throw new BaseException("新增失败");
        }
    }

    @Override
    public void update(Long id, AdminPortalPostSaveRequest request) {
        if (id == null) {
            throw new BaseException("缺少ID");
        }
        if (request == null) {
            throw new BaseException("参数错误");
        }
        PortalPost existing = adminPortalPostMapper.getById(id);
        if (existing == null) {
            throw new BaseException("内容不存在");
        }

        Long adminId = AdminContext.getCurrentId();
        PortalPost post = new PortalPost();
        post.setId(id);
        post.setType(request.getType() == null ? existing.getType() : request.getType());
        post.setTag(request.getTag() == null ? existing.getTag() : trimOrNull(request.getTag()));
        post.setTitle(request.getTitle() == null ? existing.getTitle() : request.getTitle().trim());
        post.setSubtitle(request.getSubtitle() == null ? existing.getSubtitle() : trimOrNull(request.getSubtitle()));
        post.setContent(request.getContent() == null ? existing.getContent() : request.getContent());
        post.setCoverUrl(request.getCoverUrl() == null ? existing.getCoverUrl() : trimOrNull(request.getCoverUrl()));
        post.setAccent(request.getAccent() == null ? existing.getAccent() : trimOrNull(request.getAccent()));
        post.setSort(request.getSort() == null ? existing.getSort() : request.getSort());
        post.setPublishTime(request.getPublishTime());
        post.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        post.setUpdateUser(adminId);

        int updated = adminPortalPostMapper.update(post);
        if (updated != 1) {
            throw new BaseException("更新失败");
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BaseException("缺少ID");
        }
        Long adminId = AdminContext.getCurrentId();
        int updated = adminPortalPostMapper.softDelete(id, adminId);
        if (updated != 1) {
            throw new BaseException("删除失败");
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
