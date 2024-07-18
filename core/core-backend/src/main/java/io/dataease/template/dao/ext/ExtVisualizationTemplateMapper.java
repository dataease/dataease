package io.dataease.template.dao.ext;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.visualization.vo.*;
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

    Long checkCategoryTemplateBatchNames(@Param("templateNames") List<String> templateNames,@Param("categories") List<String> categories,@Param("templateArray") List<String> templateArray);

    List<String> findTemplateCategories(@Param("templateId") String templateId);

    List<String> findTemplateArrayCategories(@Param("templateArray") List<String> templateArray);

    List<AppCoreChartViewVO> findAppViewInfo(@Param("viewIds") List<Long> viewIds);

    List<AppCoreDatasetGroupVO> findAppDatasetGroupInfo(@Param("dsIds") List<Long> dsIds);

    List<AppCoreDatasetTableVO> findAppDatasetTableInfo(@Param("dsIds") List<Long> dsIds);

    List<AppCoreDatasetTableFieldVO> findAppDatasetTableFieldInfo(@Param("dsIds") List<Long> dsIds);

    List<AppCoreDatasourceVO> findAppDatasourceInfo(@Param("dsIds") List<Long> dsIds);

    List<AppCoreDatasourceTaskVO> findAppDatasourceTaskInfo(@Param("dsIds") List<Long> dsIds);

    List<VisualizationLinkageVO> findAppLinkageInfo(@Param("dvId") Long dvId);

    List<VisualizationLinkageFieldVO> findAppLinkageFieldInfo(@Param("dvId") Long dvId);

    List<VisualizationLinkJumpVO> findAppLinkJumpInfo(@Param("dvId") Long dvId);

    List<VisualizationLinkJumpInfoVO> findAppLinkJumpInfoInfo(@Param("dvId") Long dvId);

    List<VisualizationLinkJumpTargetViewInfoVO> findAppLinkJumpTargetViewInfoInfo(@Param("dvId") Long dvId);

}
