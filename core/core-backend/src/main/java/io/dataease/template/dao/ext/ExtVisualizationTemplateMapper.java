package io.dataease.template.dao.ext;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ExtVisualizationTemplateMapper{

    List<TemplateManageDTO> findTemplateList(TemplateManageRequest request);
    List<TemplateManageDTO> findBaseTemplateList(@Param("nodeType") String nodeType);

}
