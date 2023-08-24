package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelViewLinkageField;
import io.dataease.plugins.common.base.domain.PanelViewLinkageFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelViewLinkageFieldMapper {
    long countByExample(PanelViewLinkageFieldExample example);

    int deleteByExample(PanelViewLinkageFieldExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelViewLinkageField record);

    int insertSelective(PanelViewLinkageField record);

    List<PanelViewLinkageField> selectByExample(PanelViewLinkageFieldExample example);

    PanelViewLinkageField selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelViewLinkageField record, @Param("example") PanelViewLinkageFieldExample example);

    int updateByExample(@Param("record") PanelViewLinkageField record, @Param("example") PanelViewLinkageFieldExample example);

    int updateByPrimaryKeySelective(PanelViewLinkageField record);

    int updateByPrimaryKey(PanelViewLinkageField record);
}