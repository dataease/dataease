package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetRowPermissions;
import io.dataease.base.domain.DatasetRowPermissionsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetRowPermissionsMapper {
    long countByExample(DatasetRowPermissionsExample example);

    int deleteByExample(DatasetRowPermissionsExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetRowPermissions record);

    int insertSelective(DatasetRowPermissions record);

    List<DatasetRowPermissions> selectByExample(DatasetRowPermissionsExample example);

    DatasetRowPermissions selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetRowPermissions record, @Param("example") DatasetRowPermissionsExample example);

    int updateByExample(@Param("record") DatasetRowPermissions record, @Param("example") DatasetRowPermissionsExample example);

    int updateByPrimaryKeySelective(DatasetRowPermissions record);

    int updateByPrimaryKey(DatasetRowPermissions record);
}