package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParamsInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationOuterParamsInfoRepository extends JpaRepository<SnapshotVisualizationOuterParamsInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParamsInfo> {

    @Transactional
    default void deleteByParamsIds(List<String> paramsIds) {
        Specification<SnapshotVisualizationOuterParamsInfo> spec = (root, query, cb) ->
                cb.and(cb.in(root.get("paramsId")).value(paramsIds));
        List<SnapshotVisualizationOuterParamsInfo> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

}
