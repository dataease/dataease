package io.dataease.base.mapper;

import io.dataease.base.domain.PanelOuterParamsInfo;
import io.dataease.base.domain.PanelOuterParamsInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelOuterParamsInfoMapper {
    long countByExample(PanelOuterParamsInfoExample example);

    int deleteByExample(PanelOuterParamsInfoExample example);

    int deleteByPrimaryKey(String paramsInfoId);

    int insert(PanelOuterParamsInfo record);

    int insertSelective(PanelOuterParamsInfo record);

    List<PanelOuterParamsInfo> selectByExample(PanelOuterParamsInfoExample example);

    PanelOuterParamsInfo selectByPrimaryKey(String paramsInfoId);

    int updateByExampleSelective(@Param("record") PanelOuterParamsInfo record, @Param("example") PanelOuterParamsInfoExample example);

    int updateByExample(@Param("record") PanelOuterParamsInfo record, @Param("example") PanelOuterParamsInfoExample example);

    int updateByPrimaryKeySelective(PanelOuterParamsInfo record);

    int updateByPrimaryKey(PanelOuterParamsInfo record);
}