package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelView;
import io.dataease.plugins.common.base.domain.PanelViewExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelViewMapper {
    long countByExample(PanelViewExample example);

    int deleteByExample(PanelViewExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelView record);

    int insertSelective(PanelView record);

    List<PanelView> selectByExampleWithBLOBs(PanelViewExample example);

    List<PanelView> selectByExample(PanelViewExample example);

    PanelView selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelView record, @Param("example") PanelViewExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelView record, @Param("example") PanelViewExample example);

    int updateByExample(@Param("record") PanelView record, @Param("example") PanelViewExample example);

    int updateByPrimaryKeySelective(PanelView record);

    int updateByPrimaryKeyWithBLOBs(PanelView record);

    int updateByPrimaryKey(PanelView record);
}