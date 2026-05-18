package com.programmer.escrow.chat.mapper;

import com.programmer.escrow.chat.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessageEntity entity);

    ChatMessageEntity selectById(@Param("id") Long id);

    List<ChatMessageEntity> selectByUserId(@Param("userId") Long userId);

    List<ChatMessageEntity> selectAll();

    List<ChatMessageEntity> selectConversationMessages(@Param("bizType") Integer bizType,
                                                       @Param("demandId") Long demandId,
                                                       @Param("orderId") Long orderId,
                                                       @Param("leftUserId") Long leftUserId,
                                                       @Param("rightUserId") Long rightUserId);

    int markConversationRead(@Param("bizType") Integer bizType,
                             @Param("demandId") Long demandId,
                             @Param("orderId") Long orderId,
                             @Param("receiverId") Long receiverId,
                             @Param("senderId") Long senderId,
                             @Param("readAt") LocalDateTime readAt);

    int updateRecalled(@Param("id") Long id,
                       @Param("senderId") Long senderId,
                       @Param("readAt") LocalDateTime readAt);
}
