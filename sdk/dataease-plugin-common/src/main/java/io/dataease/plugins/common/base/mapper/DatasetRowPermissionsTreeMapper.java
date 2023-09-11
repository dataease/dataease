package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetRowPermissionsTree;
import io.dataease.plugins.common.base.domain.DatasetRowPermissionsTreeExample;
import io.dataease.plugins.common.base.domain.DatasetRowPermissionsTreeWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatasetRowPermissionsTreeMapper {
    long countByExample(DatasetRowPermissionsTreeExample example);

    int deleteByExample(DatasetRowPermissionsTreeExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetRowPermissionsTreeWithBLOBs record);

    int insertSelective(DatasetRowPermissionsTreeWithBLOBs record);

    List<DatasetRowPermissionsTreeWithBLOBs> selectByExampleWithBLOBs(DatasetRowPermissionsTreeExample example);

    List<DatasetRowPermissionsTree> selectByExample(DatasetRowPermissionsTreeExample example);

    DatasetRowPermissionsTreeWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetRowPermissionsTreeWithBLOBs record, @Param("example") DatasetRowPermissionsTreeExample example);

    int updateByExampleWithBLOBs(@Param("record") DatasetRowPermissionsTreeWithBLOBs record, @Param("example") DatasetRowPermissionsTreeExample example);

    int updateByExample(@Param("record") DatasetRowPermissionsTree record, @Param("example") DatasetRowPermissionsTreeExample example);

    int updateByPrimaryKeySelective(DatasetRowPermissionsTreeWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(DatasetRowPermissionsTreeWithBLOBs record);

    int updateByPrimaryKey(DatasetRowPermissionsTree record);
}
