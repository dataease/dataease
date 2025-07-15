package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParamsTargetViewInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationOuterParamsTargetViewInfoRepository extends JpaRepository<SnapshotVisualizationOuterParamsTargetViewInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParamsTargetViewInfo> {


    @Transactional
    default void deleteByParamsInfoIds(List<String> paramsInfoIds) {
        Specification<SnapshotVisualizationOuterParamsTargetViewInfo> spec = (root, query, cb) ->
                cb.and(root.get("paramsInfoId").in(paramsInfoIds));
        List<SnapshotVisualizationOuterParamsTargetViewInfo> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

}
