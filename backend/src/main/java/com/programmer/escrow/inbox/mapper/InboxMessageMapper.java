package com.programmer.escrow.inbox.mapper;

import com.programmer.escrow.inbox.entity.InboxMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InboxMessageMapper {

    int insert(InboxMessageEntity entity);

    InboxMessageEntity selectById(@Param("id") Long id);

    List<InboxMessageEntity> selectByUserId(@Param("userId") Long userId);

    int countUnreadByUserId(@Param("userId") Long userId);

    InboxMessageEntity selectLatestUnreadByUserId(@Param("userId") Long userId);

    int updateRead(@Param("id") Long id,
                   @Param("userId") Long userId,
                   @Param("readAt") LocalDateTime readAt);

    int updateAllRead(@Param("userId") Long userId,
                      @Param("readAt") LocalDateTime readAt);
}
