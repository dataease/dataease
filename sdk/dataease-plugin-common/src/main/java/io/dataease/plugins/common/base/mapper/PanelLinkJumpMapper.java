package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelLinkJump;
import io.dataease.plugins.common.base.domain.PanelLinkJumpExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelLinkJumpMapper {
    long countByExample(PanelLinkJumpExample example);

    int deleteByExample(PanelLinkJumpExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelLinkJump record);

    int insertSelective(PanelLinkJump record);

    List<PanelLinkJump> selectByExample(PanelLinkJumpExample example);

    PanelLinkJump selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelLinkJump record, @Param("example") PanelLinkJumpExample example);

    int updateByExample(@Param("record") PanelLinkJump record, @Param("example") PanelLinkJumpExample example);

    int updateByPrimaryKeySelective(PanelLinkJump record);

    int updateByPrimaryKey(PanelLinkJump record);
}