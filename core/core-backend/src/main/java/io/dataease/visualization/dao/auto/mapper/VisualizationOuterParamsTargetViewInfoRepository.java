package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationOuterParamsTargetViewInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationOuterParamsTargetViewInfoRepository extends JpaRepository<VisualizationOuterParamsTargetViewInfo, Long>, JpaSpecificationExecutor<VisualizationOuterParamsTargetViewInfo> {


    @Transactional
    default void deleteByParamsInfoIds(List<String> paramsInfoIds) {
        Specification<VisualizationOuterParamsTargetViewInfo> spec = (root, query, cb) ->
                cb.and(root.get("paramsInfoId").in(paramsInfoIds));
        List<VisualizationOuterParamsTargetViewInfo> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

}
