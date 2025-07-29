package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.VisualizationTemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface VisualizationTemplateCategoryRepository extends JpaRepository<VisualizationTemplateCategory, String>, JpaSpecificationExecutor<VisualizationTemplateCategory> {


}
