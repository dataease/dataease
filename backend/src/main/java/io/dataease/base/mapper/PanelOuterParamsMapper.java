package io.dataease.base.mapper;

import io.dataease.base.domain.PanelOuterParams;
import io.dataease.base.domain.PanelOuterParamsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelOuterParamsMapper {
    long countByExample(PanelOuterParamsExample example);

    int deleteByExample(PanelOuterParamsExample example);

    int deleteByPrimaryKey(String paramsId);

    int insert(PanelOuterParams record);

    int insertSelective(PanelOuterParams record);

    List<PanelOuterParams> selectByExample(PanelOuterParamsExample example);

    PanelOuterParams selectByPrimaryKey(String paramsId);

    int updateByExampleSelective(@Param("record") PanelOuterParams record, @Param("example") PanelOuterParamsExample example);

    int updateByExample(@Param("record") PanelOuterParams record, @Param("example") PanelOuterParamsExample example);

    int updateByPrimaryKeySelective(PanelOuterParams record);

    int updateByPrimaryKey(PanelOuterParams record);
}