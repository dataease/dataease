package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParams;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationOuterParamsRepository extends JpaRepository<SnapshotVisualizationOuterParams, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParams> {

    @Transactional
    default void deleteByVisualizationId(String visualizationId) {
        Specification<SnapshotVisualizationOuterParams> spec = (root, query, cb) ->
                cb.equal(root.get("visualizationId"), visualizationId);
        List<SnapshotVisualizationOuterParams> list = findAll(spec);
        if (!list.isEmpty()) {
            deleteAll(list);
        }
    }

}
