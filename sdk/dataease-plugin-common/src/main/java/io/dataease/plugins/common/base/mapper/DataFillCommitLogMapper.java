package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DataFillCommitLog;
import io.dataease.plugins.common.base.domain.DataFillCommitLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataFillCommitLogMapper {
    long countByExample(DataFillCommitLogExample example);

    int deleteByExample(DataFillCommitLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataFillCommitLog record);

    int insertSelective(DataFillCommitLog record);

    List<DataFillCommitLog> selectByExample(DataFillCommitLogExample example);

    DataFillCommitLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataFillCommitLog record, @Param("example") DataFillCommitLogExample example);

    int updateByExample(@Param("record") DataFillCommitLog record, @Param("example") DataFillCommitLogExample example);

    int updateByPrimaryKeySelective(DataFillCommitLog record);

    int updateByPrimaryKey(DataFillCommitLog record);
}