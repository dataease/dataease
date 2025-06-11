package io.dataease.visualization.dao.auto.mapper;


import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.visualization.dao.auto.entity.SnapshotCoreChartView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotCoreChartViewRepository extends JpaRepository<SnapshotCoreChartView, Long>, JpaSpecificationExecutor<SnapshotCoreChartView> {

    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotCoreChartView c WHERE c.sceneId = :sceneId ")
    void deleteBySceneId(Long sceneId);

    @Query("SELECT c FROM SnapshotCoreChartView c WHERE c.sceneId = :sceneId")
    List<SnapshotCoreChartView> findBySceneId(Long sceneId);

}
