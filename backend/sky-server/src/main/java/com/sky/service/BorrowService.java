package com.sky.service;

import com.sky.vo.BorrowedBookVO;

import java.util.List;

public interface BorrowService {

    void borrow(Long bookId);

    void returnBook(Long recordId);

    void renew(Long recordId);

    List<BorrowedBookVO> listCurrent();
}

