package com.sky.service.admin.impl;

import com.sky.context.AdminContext;
import com.sky.exception.BaseException;
import com.sky.mapper.admin.AdminUserMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.AdminUserService;
import com.sky.utils.PasswordUtil;
import com.sky.vo.AdminReaderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final String DEFAULT_PASSWORD = "111111aA";

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public PageResult pageReaders(String keyword, Integer status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (p - 1) * ps;

        long total = adminUserMapper.countReaders(keyword, status);
        List<AdminReaderVO> records = total == 0 ? Collections.emptyList() : adminUserMapper.listReaders(keyword, status, offset, ps);
        return new PageResult(total, records);
    }

    @Override
    public void resetPassword(Long id) {
        if (id == null) {
            throw new BaseException("缺少用户ID");
        }
        Long adminId = AdminContext.getCurrentId();
        int updated = adminUserMapper.updatePasswordHash(id, PasswordUtil.sha256Hex(DEFAULT_PASSWORD), adminId);
        if (updated != 1) {
            throw new BaseException("重置失败");
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BaseException("缺少用户ID");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BaseException("状态不合法");
        }
        Long adminId = AdminContext.getCurrentId();
        int updated = adminUserMapper.updateStatus(id, status, adminId);
        if (updated != 1) {
            throw new BaseException("更新失败");
        }
    }
}
