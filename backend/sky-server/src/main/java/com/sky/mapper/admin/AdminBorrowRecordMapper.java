package com.sky.mapper.admin;

import com.sky.vo.AdminBorrowRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AdminBorrowRecordMapper {

    long count(@Param("status") Integer status, @Param("keyword") String keyword);

    List<AdminBorrowRecordVO> list(@Param("status") Integer status,
                                   @Param("keyword") String keyword,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    long countActiveByBookId(@Param("bookId") Long bookId);

    int insertBorrowRecord(@Param("userId") Long userId,
                           @Param("bookId") Long bookId,
                           @Param("borrowDays") Integer borrowDays,
                           @Param("handledBy") Long handledBy);

    Long getBookIdByRecordId(@Param("recordId") Long recordId);

    int returnBook(@Param("recordId") Long recordId,
                   @Param("fineAmount") BigDecimal fineAmount,
                   @Param("handledBy") Long handledBy);
}

