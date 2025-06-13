package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationOuterParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface VisualizationOuterParamsRepository extends JpaRepository<VisualizationOuterParams, Long>, JpaSpecificationExecutor<VisualizationOuterParams> {


    List<VisualizationOuterParams> findByVisualizationId(String visualizationId);
}
