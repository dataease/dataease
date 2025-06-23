package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.VisualizationTemplateCategoryMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationTemplateCategoryMapRepository extends JpaRepository<VisualizationTemplateCategoryMap, String>, JpaSpecificationExecutor<VisualizationTemplateCategoryMap> {


    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationTemplateCategoryMap vtcm WHERE vtcm.templateId = :templateId AND vtcm.categoryId = :categoryId")
    int deleteByTemplateIdAndCategoryId(String templateId, String categoryId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationTemplateCategoryMap vtcm WHERE vtcm.categoryId = :categoryId")
    int deleteByCategoryId(String categoryId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationTemplateCategoryMap vtcm WHERE vtcm.templateId = :templateId")
    int deleteByTemplateId(String templateId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationTemplateCategoryMap vtcm WHERE vtcm.templateId in :templateIds")
    int deleteByTemplateIds(List<String> templateIds);

    @Query("SELECT DISTINCT vtcm.categoryId " +
            "FROM VisualizationTemplateCategoryMap vtcm " +
            "WHERE vtcm.templateId IN :templateIds " +
            "GROUP BY vtcm.templateId")
    List<String> findTemplateArrayCategories(List<String> templateIds);

}
