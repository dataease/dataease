package io.dataease.ext;

import io.dataease.plugins.common.base.domain.PanelAppTemplateWithBLOBs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ExtPanelAppTemplateMapper {

    List<PanelAppTemplateWithBLOBs> queryBaseInfo(@Param("nodeType") String nodeType , @Param("pid") String pid);
}
