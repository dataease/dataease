package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJumpTargetViewInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
