package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelDesign;
import io.dataease.plugins.common.base.domain.PanelDesignExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelDesignMapper {
    long countByExample(PanelDesignExample example);

    int deleteByExample(PanelDesignExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelDesign record);

    int insertSelective(PanelDesign record);

    List<PanelDesign> selectByExample(PanelDesignExample example);

    PanelDesign selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelDesign record, @Param("example") PanelDesignExample example);

    int updateByExample(@Param("record") PanelDesign record, @Param("example") PanelDesignExample example);

    int updateByPrimaryKeySelective(PanelDesign record);

    int updateByPrimaryKey(PanelDesign record);
}