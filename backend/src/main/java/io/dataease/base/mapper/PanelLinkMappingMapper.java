package io.dataease.base.mapper;

import io.dataease.base.domain.PanelLinkMapping;
import io.dataease.base.domain.PanelLinkMappingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelLinkMappingMapper {
    long countByExample(PanelLinkMappingExample example);

    int deleteByExample(PanelLinkMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PanelLinkMapping record);

    int insertSelective(PanelLinkMapping record);

    List<PanelLinkMapping> selectByExample(PanelLinkMappingExample example);

    PanelLinkMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PanelLinkMapping record, @Param("example") PanelLinkMappingExample example);

    int updateByExample(@Param("record") PanelLinkMapping record, @Param("example") PanelLinkMappingExample example);

    int updateByPrimaryKeySelective(PanelLinkMapping record);

    int updateByPrimaryKey(PanelLinkMapping record);
}