package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJumpInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

}
