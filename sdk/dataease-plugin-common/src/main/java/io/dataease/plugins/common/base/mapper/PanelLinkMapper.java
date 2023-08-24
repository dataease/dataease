package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelLink;
import io.dataease.plugins.common.base.domain.PanelLinkExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelLinkMapper {
    long countByExample(PanelLinkExample example);

    int deleteByExample(PanelLinkExample example);

    int insert(PanelLink record);

    int insertSelective(PanelLink record);

    List<PanelLink> selectByExample(PanelLinkExample example);

    int updateByExampleSelective(@Param("record") PanelLink record, @Param("example") PanelLinkExample example);

    int updateByExample(@Param("record") PanelLink record, @Param("example") PanelLinkExample example);
}