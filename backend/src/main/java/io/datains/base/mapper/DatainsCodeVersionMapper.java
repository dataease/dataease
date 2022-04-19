package io.datains.base.mapper;

import io.datains.base.domain.DatainsCodeVersion;
import io.datains.base.domain.DatainsCodeVersionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatainsCodeVersionMapper {
    long countByExample(DatainsCodeVersionExample example);

    int deleteByExample(DatainsCodeVersionExample example);

    int deleteByPrimaryKey(Integer installedRank);

    int insert(DatainsCodeVersion record);

    int insertSelective(DatainsCodeVersion record);

    List<DatainsCodeVersion> selectByExample(DatainsCodeVersionExample example);

    DatainsCodeVersion selectByPrimaryKey(Integer installedRank);

    int updateByExampleSelective(@Param("record") DatainsCodeVersion record, @Param("example") DatainsCodeVersionExample example);

    int updateByExample(@Param("record") DatainsCodeVersion record, @Param("example") DatainsCodeVersionExample example);

    int updateByPrimaryKeySelective(DatainsCodeVersion record);

    int updateByPrimaryKey(DatainsCodeVersion record);
}