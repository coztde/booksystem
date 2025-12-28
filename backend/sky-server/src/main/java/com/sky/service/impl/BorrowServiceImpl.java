package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.Book;
import com.sky.entity.ReaderType;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.BookMapper;
import com.sky.mapper.BorrowRecordMapper;
import com.sky.mapper.ReaderTypeMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.BorrowService;
import com.sky.vo.BorrowedBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReaderTypeMapper readerTypeMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    @Transactional
    public void borrow(Long bookId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        if (bookId == null) {
            throw new BaseException("请选择图书");
        }

        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        ReaderType readerType = readerTypeMapper.getById(user.getReaderTypeId());
        if (readerType == null) {
            throw new BaseException("读者类型不存在");
        }

        long activeCount = borrowRecordMapper.countActiveByUserId(userId);
        if (activeCount >= readerType.getMaxBorrow()) {
            throw new BaseException("已达到最大可借数量");
        }

        Book book = bookMapper.getById(bookId);
        if (book == null || book.getIsDeleted() != null && book.getIsDeleted() == 1) {
            throw new BaseException("图书不存在");
        }
        if (book.getStatus() != null && book.getStatus() == 0) {
            throw new BaseException("图书不可借");
        }

        int updated = bookMapper.decrementAvailableQty(bookId);
        if (updated != 1) {
            throw new BaseException("库存不足");
        }
        int inserted = borrowRecordMapper.insertBorrowRecord(userId, bookId, readerType.getBorrowDays());
        if (inserted != 1) {
            throw new BaseException("借阅失败");
        }
    }

    @Override
    @Transactional
    public void returnBook(Long recordId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        if (recordId == null) {
            throw new BaseException("请选择借阅记录");
        }

        Long bookId = borrowRecordMapper.getBookIdByRecordId(recordId, userId);
        if (bookId == null) {
            throw new BaseException("借阅记录不存在");
        }

        int updated = borrowRecordMapper.returnBook(recordId, userId);
        if (updated != 1) {
            throw new BaseException("归还失败");
        }

        bookMapper.incrementAvailableQty(bookId);
    }

    @Override
    @Transactional
    public void renew(Long recordId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        if (recordId == null) {
            throw new BaseException("请选择借阅记录");
        }

        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        ReaderType readerType = readerTypeMapper.getById(user.getReaderTypeId());
        if (readerType == null) {
            throw new BaseException("读者类型不存在");
        }

        int updated = borrowRecordMapper.renew(recordId, userId, readerType.getBorrowDays(), readerType.getMaxRenew());
        if (updated != 1) {
            throw new BaseException("续借失败（可能已超期或达到最大续借次数）");
        }
    }

    @Override
    public List<BorrowedBookVO> listCurrent() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        return borrowRecordMapper.listBorrowedByUserId(userId);
    }
}

