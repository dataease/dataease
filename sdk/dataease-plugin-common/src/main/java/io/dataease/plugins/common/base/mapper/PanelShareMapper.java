package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelShare;
import io.dataease.plugins.common.base.domain.PanelShareExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelShareMapper {
    long countByExample(PanelShareExample example);

    int deleteByExample(PanelShareExample example);

    int deleteByPrimaryKey(Long shareId);

    int insert(PanelShare record);

    int insertSelective(PanelShare record);

    List<PanelShare> selectByExample(PanelShareExample example);

    PanelShare selectByPrimaryKey(Long shareId);

    int updateByExampleSelective(@Param("record") PanelShare record, @Param("example") PanelShareExample example);

    int updateByExample(@Param("record") PanelShare record, @Param("example") PanelShareExample example);

    int updateByPrimaryKeySelective(PanelShare record);

    int updateByPrimaryKey(PanelShare record);
}