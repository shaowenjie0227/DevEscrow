package com.programmer.escrow.notice.mapper;

import com.programmer.escrow.notice.entity.HomeNoticeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeNoticeMapper {
    int insert(HomeNoticeEntity entity);
    int update(HomeNoticeEntity entity);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    HomeNoticeEntity selectById(@Param("id") Long id);
    List<HomeNoticeEntity> selectActiveList();
    List<HomeNoticeEntity> selectAdminList();
}
