package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.VisualizationTemplateExtendData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface VisualizationTemplateExtendDataRepository extends JpaRepository<VisualizationTemplateExtendData, Long>, JpaSpecificationExecutor<VisualizationTemplateExtendData> {

    List<VisualizationTemplateExtendData> findByViewId(Long viewId);

}
