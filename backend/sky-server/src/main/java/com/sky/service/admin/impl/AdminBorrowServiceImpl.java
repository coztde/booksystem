package com.sky.service.admin.impl;

import com.sky.context.AdminContext;
import com.sky.dto.AdminBorrowCreateRequest;
import com.sky.dto.AdminBorrowReturnRequest;
import com.sky.entity.Book;
import com.sky.entity.ReaderType;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.BookMapper;
import com.sky.mapper.BorrowRecordMapper;
import com.sky.mapper.ReaderTypeMapper;
import com.sky.mapper.UserMapper;
import com.sky.mapper.admin.AdminBorrowRecordMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.AdminBorrowService;
import com.sky.vo.AdminBorrowRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class AdminBorrowServiceImpl implements AdminBorrowService {

    @Autowired
    private AdminBorrowRecordMapper adminBorrowRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReaderTypeMapper readerTypeMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    public PageResult page(Integer status, String keyword, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (p - 1) * ps;

        long total = adminBorrowRecordMapper.count(status, keyword);
        List<AdminBorrowRecordVO> records =
                total == 0 ? Collections.emptyList() : adminBorrowRecordMapper.list(status, keyword, offset, ps);
        return new PageResult(total, records);
    }

    @Override
    @Transactional
    public void borrow(AdminBorrowCreateRequest request) {
        if (request == null) {
            throw new BaseException("参数错误");
        }
        String userCode = request.getUserCode() == null ? null : request.getUserCode().trim();
        if (userCode == null || userCode.isEmpty()) {
            throw new BaseException("请输入读者学号/工号");
        }
        Long bookId = request.getBookId();
        if (bookId == null) {
            throw new BaseException("请选择图书");
        }

        User user = userMapper.getReaderByCode(userCode);
        if (user == null) {
            throw new BaseException("读者不存在或已禁用");
        }
        ReaderType readerType = readerTypeMapper.getById(user.getReaderTypeId());
        if (readerType == null) {
            throw new BaseException("读者类型不存在");
        }

        long activeCount = borrowRecordMapper.countActiveByUserId(user.getId());
        if (activeCount >= readerType.getMaxBorrow()) {
            throw new BaseException("该读者已达到最大可借数量");
        }

        Book book = bookMapper.getById(bookId);
        if (book == null || (book.getIsDeleted() != null && book.getIsDeleted() == 1)) {
            throw new BaseException("图书不存在");
        }
        if (book.getStatus() != null && book.getStatus() == 0) {
            throw new BaseException("图书不可借");
        }

        int updated = bookMapper.decrementAvailableQty(bookId);
        if (updated != 1) {
            throw new BaseException("库存不足");
        }

        Long adminId = AdminContext.getCurrentId();
        int inserted = adminBorrowRecordMapper.insertBorrowRecord(user.getId(), bookId, readerType.getBorrowDays(), adminId);
        if (inserted != 1) {
            throw new BaseException("借出失败");
        }
    }

    @Override
    @Transactional
    public void returnBook(AdminBorrowReturnRequest request) {
        if (request == null || request.getRecordId() == null) {
            throw new BaseException("请选择借阅记录");
        }

        Long bookId = adminBorrowRecordMapper.getBookIdByRecordId(request.getRecordId());
        if (bookId == null) {
            throw new BaseException("借阅记录不存在");
        }

        Long adminId = AdminContext.getCurrentId();
        int updated = adminBorrowRecordMapper.returnBook(request.getRecordId(), request.getFineAmount(), adminId);
        if (updated != 1) {
            throw new BaseException("归还失败（可能已归还）");
        }

        bookMapper.incrementAvailableQty(bookId);
    }
}
