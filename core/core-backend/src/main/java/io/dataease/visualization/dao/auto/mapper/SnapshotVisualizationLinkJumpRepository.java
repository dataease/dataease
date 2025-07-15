package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJump;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationLinkJumpRepository extends JpaRepository<SnapshotVisualizationLinkJump, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkJump> {


    @Transactional
    default void deleteBySourceDvId(Long sourceDvId) {
        Specification<SnapshotVisualizationLinkJump> spec = (root, query, cb) ->
                cb.equal(root.get("sourceDvId"), sourceDvId);
        List<SnapshotVisualizationLinkJump> jumps = findAll(spec);
        if (!jumps.isEmpty()) {
            deleteAll(jumps);
        }
    }

}
