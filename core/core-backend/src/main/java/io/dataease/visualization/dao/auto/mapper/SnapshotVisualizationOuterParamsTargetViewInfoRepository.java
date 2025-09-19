package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParamsTargetViewInfo;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationOuterParamsTargetViewInfoRepository extends JpaRepository<SnapshotVisualizationOuterParamsTargetViewInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParamsTargetViewInfo> {


    @Transactional
    default void deleteByParamsInfoIds(List<Long> paramsInfoIds) {
        Specification<SnapshotVisualizationOuterParamsTargetViewInfo> spec = (root, query, cb) ->
                cb.and(root.get("paramsInfoId").in(paramsInfoIds));
        List<SnapshotVisualizationOuterParamsTargetViewInfo> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

    @Transactional
    @Modifying
    @Query("DELETE FROM SnapshotVisualizationOuterParamsTargetViewInfo poptvi " +
            "WHERE poptvi.paramsInfoId IN (" +
            "    SELECT poptvi2.paramsInfoId FROM SnapshotVisualizationOuterParamsTargetViewInfo poptvi2 " +
            "    INNER JOIN SnapshotVisualizationOuterParamsInfo popi ON poptvi2.paramsInfoId = popi.paramsInfoId " +
            "    INNER JOIN SnapshotVisualizationOuterParams pop ON popi.paramsId = pop.paramsId " +
            "    WHERE pop.visualizationId = :visualizationId" +
            ")")
    int deleteByVisualizationId(@Param("visualizationId") Long visualizationId);

}
