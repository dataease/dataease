package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJumpInfo;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationLinkJumpInfoRepository extends JpaRepository<SnapshotVisualizationLinkJumpInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkJumpInfo> {


    @Transactional
    default void deleteByLinkJumpIds(List<Long> linkJumpIds) {
        Specification<SnapshotVisualizationLinkJumpInfo> spec = (root, query, cb) ->
                root.get("linkJumpId").in(linkJumpIds);
        List<SnapshotVisualizationLinkJumpInfo> infos = findAll(spec);
        if (!infos.isEmpty()) {
            deleteAll(infos);
        }
    }

    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationLinkJumpInfo info " +
            "WHERE info.linkJumpId IN (" +
            "    SELECT lj.id FROM SnapshotVisualizationLinkJump lj " +
            "    WHERE lj.sourceDvId = :dvId " +
            "    AND lj.sourceViewId = :viewId" +
            ")")
    void deleteBySourceDvIdAndViewId(@Param("dvId") Long dvId, @Param("viewId") Long viewId);

}
