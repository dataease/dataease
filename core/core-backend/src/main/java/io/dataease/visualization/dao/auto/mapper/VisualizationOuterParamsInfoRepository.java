package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationOuterParamsInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationOuterParamsInfoRepository extends JpaRepository<VisualizationOuterParamsInfo, Long>, JpaSpecificationExecutor<VisualizationOuterParamsInfo> {


    @Transactional
    default void deleteByParamsIds(List<String> paramsIds) {
        Specification<VisualizationOuterParamsInfo> spec = (root, query, cb) ->
                cb.and(root.get("paramsId").in(paramsIds));
        List<VisualizationOuterParamsInfo> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }
}
