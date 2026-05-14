package com.programmer.escrow.user.mapper;

import com.programmer.escrow.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(UserEntity entity);

    UserEntity selectById(@Param("id") Long id);

    UserEntity selectByPhone(@Param("phone") String phone);

    UserEntity selectByEmail(@Param("email") String email);

    UserEntity selectByAccount(@Param("account") String account);

    List<UserEntity> selectAdminList(@Param("status") Integer status,
                                     @Param("userType") Integer userType,
                                     @Param("keyword") String keyword);

    int updateLastLoginAt(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
