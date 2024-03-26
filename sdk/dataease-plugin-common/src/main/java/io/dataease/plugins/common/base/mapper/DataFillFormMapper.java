package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DataFillForm;
import io.dataease.plugins.common.base.domain.DataFillFormExample;
import io.dataease.plugins.common.base.domain.DataFillFormWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataFillFormMapper {
    long countByExample(DataFillFormExample example);

    int deleteByExample(DataFillFormExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataFillFormWithBLOBs record);

    int insertSelective(DataFillFormWithBLOBs record);

    List<DataFillFormWithBLOBs> selectByExampleWithBLOBs(DataFillFormExample example);

    List<DataFillForm> selectByExample(DataFillFormExample example);

    DataFillFormWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataFillFormWithBLOBs record, @Param("example") DataFillFormExample example);

    int updateByExampleWithBLOBs(@Param("record") DataFillFormWithBLOBs record, @Param("example") DataFillFormExample example);

    int updateByExample(@Param("record") DataFillForm record, @Param("example") DataFillFormExample example);

    int updateByPrimaryKeySelective(DataFillFormWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(DataFillFormWithBLOBs record);

    int updateByPrimaryKey(DataFillForm record);
}