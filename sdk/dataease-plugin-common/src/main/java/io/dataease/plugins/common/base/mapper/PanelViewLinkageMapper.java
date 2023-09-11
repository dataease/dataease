package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelViewLinkage;
import io.dataease.plugins.common.base.domain.PanelViewLinkageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelViewLinkageMapper {
    long countByExample(PanelViewLinkageExample example);

    int deleteByExample(PanelViewLinkageExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelViewLinkage record);

    int insertSelective(PanelViewLinkage record);

    List<PanelViewLinkage> selectByExample(PanelViewLinkageExample example);

    PanelViewLinkage selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelViewLinkage record, @Param("example") PanelViewLinkageExample example);

    int updateByExample(@Param("record") PanelViewLinkage record, @Param("example") PanelViewLinkageExample example);

    int updateByPrimaryKeySelective(PanelViewLinkage record);

    int updateByPrimaryKey(PanelViewLinkage record);
}