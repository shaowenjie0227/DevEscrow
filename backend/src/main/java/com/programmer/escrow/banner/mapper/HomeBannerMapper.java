package com.programmer.escrow.banner.mapper;

import com.programmer.escrow.banner.entity.HomeBannerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeBannerMapper {
    int insert(HomeBannerEntity entity);
    int update(HomeBannerEntity entity);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    HomeBannerEntity selectById(@Param("id") Long id);
    List<HomeBannerEntity> selectActiveList();
    List<HomeBannerEntity> selectAdminList();
}
