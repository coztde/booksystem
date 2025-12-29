package com.sky.service.admin.impl;

import com.sky.context.AdminContext;
import com.sky.dto.AdminBookSaveRequest;
import com.sky.entity.Book;
import com.sky.exception.BaseException;
import com.sky.mapper.admin.AdminBookMapper;
import com.sky.mapper.admin.AdminBorrowRecordMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.AdminBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AdminBookServiceImpl implements AdminBookService {

    @Autowired
    private AdminBookMapper adminBookMapper;

    @Autowired
    private AdminBorrowRecordMapper adminBorrowRecordMapper;

    @Override
    public PageResult page(String keyword, String category, Integer status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (p - 1) * ps;

        long total = adminBookMapper.count(keyword, category, status);
        List<Book> records = total == 0 ? Collections.emptyList() : adminBookMapper.list(keyword, category, status, offset, ps);
        return new PageResult(total, records);
    }

    @Override
    public Book getById(Long id) {
        if (id == null) {
            throw new BaseException("缺少ID");
        }
        Book book = adminBookMapper.getById(id);
        if (book == null) {
            throw new BaseException("图书不存在");
        }
        return book;
    }

    @Override
    public void create(AdminBookSaveRequest request) {
        if (request == null) {
            throw new BaseException("参数错误");
        }
        String title = request.getTitle() == null ? null : request.getTitle().trim();
        if (title == null || title.isEmpty()) {
            throw new BaseException("书名不能为空");
        }
        int totalQty = request.getTotalQty() == null ? 0 : request.getTotalQty();
        if (totalQty < 0) {
            throw new BaseException("库存不能为负数");
        }

        Long adminId = AdminContext.getCurrentId();
        Book book = new Book();
        book.setCoverUrl(trimOrNull(request.getCoverUrl()));
        book.setTitle(title);
        book.setAuthor(trimOrNull(request.getAuthor()));
        book.setPublisher(trimOrNull(request.getPublisher()));
        book.setIsbn(trimOrNull(request.getIsbn()));
        book.setCategory(trimOrNull(request.getCategory()));
        book.setLocation(trimOrNull(request.getLocation()));
        book.setDescription(request.getDescription());
        book.setTotalQty(totalQty);
        book.setAvailableQty(totalQty);
        book.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        book.setCreateUser(adminId);
        book.setUpdateUser(adminId);

        int inserted = adminBookMapper.insert(book);
        if (inserted != 1) {
            throw new BaseException("新增失败");
        }
    }

    @Override
    public void update(Long id, AdminBookSaveRequest request) {
        if (id == null) {
            throw new BaseException("缺少ID");
        }
        if (request == null) {
            throw new BaseException("参数错误");
        }
        Book existing = adminBookMapper.getById(id);
        if (existing == null) {
            throw new BaseException("图书不存在");
        }

        Integer existingTotal = existing.getTotalQty() == null ? 0 : existing.getTotalQty();
        Integer existingAvailable = existing.getAvailableQty() == null ? 0 : existing.getAvailableQty();
        int borrowedQty = Math.max(0, existingTotal - existingAvailable);

        Integer newTotalQty = request.getTotalQty();
        int finalTotalQty = newTotalQty == null ? existingTotal : newTotalQty;
        if (finalTotalQty < borrowedQty) {
            throw new BaseException("总库存不能小于已借出数量(" + borrowedQty + ")");
        }
        int finalAvailableQty = newTotalQty == null ? existingAvailable : (finalTotalQty - borrowedQty);

        Long adminId = AdminContext.getCurrentId();
        Book book = new Book();
        book.setId(id);
        book.setCoverUrl(request.getCoverUrl() == null ? existing.getCoverUrl() : trimOrNull(request.getCoverUrl()));
        book.setTitle(request.getTitle() == null ? existing.getTitle() : request.getTitle().trim());
        book.setAuthor(request.getAuthor() == null ? existing.getAuthor() : trimOrNull(request.getAuthor()));
        book.setPublisher(request.getPublisher() == null ? existing.getPublisher() : trimOrNull(request.getPublisher()));
        book.setIsbn(request.getIsbn() == null ? existing.getIsbn() : trimOrNull(request.getIsbn()));
        book.setCategory(request.getCategory() == null ? existing.getCategory() : trimOrNull(request.getCategory()));
        book.setLocation(request.getLocation() == null ? existing.getLocation() : trimOrNull(request.getLocation()));
        book.setDescription(request.getDescription() == null ? existing.getDescription() : request.getDescription());
        book.setTotalQty(finalTotalQty);
        book.setAvailableQty(finalAvailableQty);
        book.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        book.setUpdateUser(adminId);

        int updated = adminBookMapper.update(book);
        if (updated != 1) {
            throw new BaseException("更新失败");
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BaseException("缺少ID");
        }
        long activeCount = adminBorrowRecordMapper.countActiveByBookId(id);
        if (activeCount > 0) {
            throw new BaseException("该图书存在未归还借阅记录，无法删除");
        }

        Long adminId = AdminContext.getCurrentId();
        int updated = adminBookMapper.softDelete(id, adminId);
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
