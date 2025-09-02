package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJumpTargetViewInfo;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationLinkJumpTargetViewInfoRepository extends JpaRepository<SnapshotVisualizationLinkJumpTargetViewInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkJumpTargetViewInfo> {


    @Transactional
    default void deleteByLinkJumpInfoIds(List<Long> linkJumpIds) {
        Specification<SnapshotVisualizationLinkJumpTargetViewInfo> spec = (root, query, cb) ->
                cb.and(root.get("linkJumpInfoId").in(linkJumpIds));
        List<SnapshotVisualizationLinkJumpTargetViewInfo> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

    @Modifying
    @Query("DELETE FROM SnapshotVisualizationLinkJumpTargetViewInfo t " +
            "WHERE t.linkJumpInfoId IN (" +
            "    SELECT lji.id FROM SnapshotVisualizationLinkJumpInfo lji " +
            "    JOIN SnapshotVisualizationLinkJump lj  ON lji.linkJumpId = lj.id " +
            "    WHERE lj.sourceDvId = :dvId " +
            "    AND lj.sourceViewId = :viewId" +
            ")")
    void deleteBySourceDvIdAndViewId(@Param("dvId") Long dvId, @Param("viewId") Long viewId);
}
