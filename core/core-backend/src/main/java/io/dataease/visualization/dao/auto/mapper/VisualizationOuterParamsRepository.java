package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationOuterParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationOuterParamsRepository extends JpaRepository<VisualizationOuterParams, Long>, JpaSpecificationExecutor<VisualizationOuterParams> {


    List<VisualizationOuterParams> findByVisualizationId(String visualizationId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationOuterParams c WHERE c.visualizationId = :visualizationId ")
    void deleteByVisualizationId(String visualizationId);
}
