package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelLinkJumpTargetViewInfo;
import io.dataease.plugins.common.base.domain.PanelLinkJumpTargetViewInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelLinkJumpTargetViewInfoMapper {
    long countByExample(PanelLinkJumpTargetViewInfoExample example);

    int deleteByExample(PanelLinkJumpTargetViewInfoExample example);

    int deleteByPrimaryKey(String targetId);

    int insert(PanelLinkJumpTargetViewInfo record);

    int insertSelective(PanelLinkJumpTargetViewInfo record);

    List<PanelLinkJumpTargetViewInfo> selectByExample(PanelLinkJumpTargetViewInfoExample example);

    PanelLinkJumpTargetViewInfo selectByPrimaryKey(String targetId);

    int updateByExampleSelective(@Param("record") PanelLinkJumpTargetViewInfo record, @Param("example") PanelLinkJumpTargetViewInfoExample example);

    int updateByExample(@Param("record") PanelLinkJumpTargetViewInfo record, @Param("example") PanelLinkJumpTargetViewInfoExample example);

    int updateByPrimaryKeySelective(PanelLinkJumpTargetViewInfo record);

    int updateByPrimaryKey(PanelLinkJumpTargetViewInfo record);
}