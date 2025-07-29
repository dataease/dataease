package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationOuterParams;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationOuterParamsRepository extends JpaRepository<VisualizationOuterParams, Long>, JpaSpecificationExecutor<VisualizationOuterParams> {


    List<VisualizationOuterParams> findByVisualizationId(String visualizationId);

    @Transactional
    default void deleteByVisualizationId(String visualizationId) {
        Specification<VisualizationOuterParams> spec = (root, query, cb) ->
                cb.equal(root.get("visualizationId"), visualizationId);
        List<VisualizationOuterParams> paramsList = findAll(spec);
        if (!paramsList.isEmpty()) {
            deleteAll(paramsList);
        }
    }
}
