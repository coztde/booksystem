package com.sky.mapper;

import com.sky.vo.BorrowedBookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BorrowRecordMapper {

    List<BorrowedBookVO> listBorrowedByUserId(@Param("userId") Long userId);

    long countActiveByUserId(@Param("userId") Long userId);

    int insertBorrowRecord(@Param("userId") Long userId,
                           @Param("bookId") Long bookId,
                           @Param("borrowDays") Integer borrowDays);

    int returnBook(@Param("recordId") Long recordId, @Param("userId") Long userId);

    int renew(@Param("recordId") Long recordId,
              @Param("userId") Long userId,
              @Param("borrowDays") Integer borrowDays,
              @Param("maxRenew") Integer maxRenew);

    Long getBookIdByRecordId(@Param("recordId") Long recordId, @Param("userId") Long userId);
}
