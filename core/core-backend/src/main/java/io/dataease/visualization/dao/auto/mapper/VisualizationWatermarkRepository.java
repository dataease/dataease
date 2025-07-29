package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface VisualizationWatermarkRepository extends JpaRepository<VisualizationWatermark, String>, JpaSpecificationExecutor<VisualizationWatermark> {



}
