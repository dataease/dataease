package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkageField;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkageFieldRepository extends JpaRepository<VisualizationLinkageField, Long>, JpaSpecificationExecutor<VisualizationLinkageField> {


    @Transactional
    default void deleteByLinkageIds(List<Long> linkageIds) {
        Specification<VisualizationLinkageField> spec = (root, query, cb) ->
                root.get("linkageId").in(linkageIds);
        List<VisualizationLinkageField> fields = findAll(spec);
        if (!fields.isEmpty()) {
            deleteAll(fields);
        }
    }

    List<VisualizationLinkageField> findByLinkageIdIn(List<Long> linkageIds);
}
