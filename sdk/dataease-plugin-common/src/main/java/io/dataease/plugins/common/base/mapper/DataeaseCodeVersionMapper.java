package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DataeaseCodeVersion;
import io.dataease.plugins.common.base.domain.DataeaseCodeVersionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataeaseCodeVersionMapper {
    long countByExample(DataeaseCodeVersionExample example);

    int deleteByExample(DataeaseCodeVersionExample example);

    int deleteByPrimaryKey(Integer installedRank);

    int insert(DataeaseCodeVersion record);

    int insertSelective(DataeaseCodeVersion record);

    List<DataeaseCodeVersion> selectByExample(DataeaseCodeVersionExample example);

    DataeaseCodeVersion selectByPrimaryKey(Integer installedRank);

    int updateByExampleSelective(@Param("record") DataeaseCodeVersion record, @Param("example") DataeaseCodeVersionExample example);

    int updateByExample(@Param("record") DataeaseCodeVersion record, @Param("example") DataeaseCodeVersionExample example);

    int updateByPrimaryKeySelective(DataeaseCodeVersion record);

    int updateByPrimaryKey(DataeaseCodeVersion record);
}