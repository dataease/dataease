package io.dataease.chart.dao.auto.mapper;


import io.dataease.dao.auto.entity.CoreChartView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


public interface CoreChartViewRepository extends JpaRepository<CoreChartView, Long>, JpaSpecificationExecutor<CoreChartView> {

    @Query("SELECT COUNT(DISTINCT c.tableId) FROM CoreChartView c WHERE c.id IN :ids")
    Long countDistinctTableIdByIdIn(List<String> ids);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreChartView c WHERE c.sceneId = :sceneId AND c.id NOT IN :chartIds")
    void deleteBySceneIdAndNotInIds(Long sceneId, List<Long> chartIds);

    @Query("SELECT c FROM CoreChartView c WHERE c.type = 'table-pivot'")
    List<CoreChartView> findAllTablePivotViews();

    @Modifying
    @Query("UPDATE CoreChartView c SET c.xAxis = :newXAxis, c.xAxisExt = :newXAxisExt WHERE c.id = :id")
    void updateAxes(@Param("id") Long id,
                    @Param("newXAxis") String newXAxis,
                    @Param("newXAxisExt") String newXAxisExt);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreChartView c WHERE c.sceneId = :sceneId ")
    void deleteBySceneId(Long sceneId);

    @Query("SELECT c FROM CoreChartView c WHERE c.sceneId = :sceneId")
    List<CoreChartView> findBySceneId(Long sceneId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreChartView c WHERE c.sceneId IN :sceneIds ")
    void deleteBySceneIds(Set<Long> sceneIds);

    @Query("SELECT c FROM CoreChartView c WHERE c.copyId = :copyId")
    List<CoreChartView> findViewInfoByCopyId(Long copyId);

    List<CoreChartView> findByIdInAndTypeNot(List<Long> ids, String type);
}
