package com.programmer.escrow.demand.mapper;

import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DemandCategoryMapper {

    int insert(DemandCategoryEntity entity);

    DemandCategoryEntity selectById(@Param("id") Long id);

    DemandCategoryEntity selectByName(@Param("categoryName") String categoryName);

    List<DemandCategoryEntity> selectActiveList();

    List<DemandCategoryEntity> selectAdminList();

    int update(DemandCategoryEntity entity);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);
}
