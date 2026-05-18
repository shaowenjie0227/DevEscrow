package com.programmer.escrow.user.mapper;

import com.programmer.escrow.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    int insert(UserEntity entity);

    UserEntity selectById(@Param("id") Long id);

    UserEntity selectByPhone(@Param("phone") String phone);

    UserEntity selectByEmail(@Param("email") String email);

    UserEntity selectByOpenid(@Param("openid") String openid);

    UserEntity selectByAccount(@Param("account") String account);

    List<UserEntity> selectAdminList(@Param("status") Integer status,
                                     @Param("userType") Integer userType,
                                     @Param("keyword") String keyword);

    long countByStatus(@Param("status") Integer status);

    long countCertifiedDevelopers(@Param("status") Integer status,
                                  @Param("developerStatus") Integer developerStatus);

    List<UserEntity> selectExpiredBannedUsers();

    int updateLastLoginAt(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateBanStatus(@Param("id") Long id,
                        @Param("status") Integer status,
                        @Param("banReason") String banReason,
                        @Param("banExpiresAt") LocalDateTime banExpiresAt);

    int clearBanStatus(@Param("id") Long id);

    int releaseExpiredBanById(@Param("id") Long id);

    int updateDeveloperProfile(UserEntity entity);

    int updateBasicProfile(UserEntity entity);

    int updateWechatProfile(UserEntity entity);
}
