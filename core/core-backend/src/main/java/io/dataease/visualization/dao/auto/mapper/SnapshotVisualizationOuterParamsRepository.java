package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface SnapshotVisualizationOuterParamsRepository extends JpaRepository<SnapshotVisualizationOuterParams, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParams> {

    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationOuterParams c WHERE c.visualizationId = :visualizationId ")
    void deleteByVisualizationId(String visualizationId);

}
