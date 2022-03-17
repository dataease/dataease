package io.dataease.base.mapper;

import io.dataease.base.domain.PanelOuterParamsTargetViewInfo;
import io.dataease.base.domain.PanelOuterParamsTargetViewInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelOuterParamsTargetViewInfoMapper {
    long countByExample(PanelOuterParamsTargetViewInfoExample example);

    int deleteByExample(PanelOuterParamsTargetViewInfoExample example);

    int deleteByPrimaryKey(String targetId);

    int insert(PanelOuterParamsTargetViewInfo record);

    int insertSelective(PanelOuterParamsTargetViewInfo record);

    List<PanelOuterParamsTargetViewInfo> selectByExample(PanelOuterParamsTargetViewInfoExample example);

    PanelOuterParamsTargetViewInfo selectByPrimaryKey(String targetId);

    int updateByExampleSelective(@Param("record") PanelOuterParamsTargetViewInfo record, @Param("example") PanelOuterParamsTargetViewInfoExample example);

    int updateByExample(@Param("record") PanelOuterParamsTargetViewInfo record, @Param("example") PanelOuterParamsTargetViewInfoExample example);

    int updateByPrimaryKeySelective(PanelOuterParamsTargetViewInfo record);

    int updateByPrimaryKey(PanelOuterParamsTargetViewInfo record);
}