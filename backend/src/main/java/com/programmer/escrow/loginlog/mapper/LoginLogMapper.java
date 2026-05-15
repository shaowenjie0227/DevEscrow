package com.programmer.escrow.loginlog.mapper;

import com.programmer.escrow.loginlog.entity.LoginLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper {

    int insert(LoginLogEntity entity);
}
