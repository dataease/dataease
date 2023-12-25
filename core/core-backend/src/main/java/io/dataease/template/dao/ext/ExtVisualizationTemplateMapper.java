package io.dataease.template.dao.ext;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ExtVisualizationTemplateMapper{

    List<TemplateManageDTO> findTemplateList(TemplateManageRequest request);

    List<TemplateManageDTO> findCategories(TemplateManageRequest request);

    List<TemplateManageDTO> findBaseTemplateList();

    Long checkCategoryMap(@Param("categoryId") String categoryId);

    Long checkRepeatTemplateId(@Param("categoryId") String categoryId, @Param("templateId") String templateId);

    void deleteCategoryMapByTemplate(@Param("templateName") String templateName, @Param("templateId") String templateId);

    Long checkCategoryTemplateName(@Param("templateName") String templateName,@Param("categories") List<String> categories);

    List<String> findTemplateCategories(@Param("templateId") String templateId);

}
