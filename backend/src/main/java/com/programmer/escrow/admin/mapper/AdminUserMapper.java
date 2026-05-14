package com.programmer.escrow.admin.mapper;

import com.programmer.escrow.admin.entity.AdminUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminUserMapper {

    AdminUserEntity selectByUsername(@Param("username") String username);

    AdminUserEntity selectById(@Param("id") Long id);

    int updateLastLoginAt(@Param("id") Long id);
}
