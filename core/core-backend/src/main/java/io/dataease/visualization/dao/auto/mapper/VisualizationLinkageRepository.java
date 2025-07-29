package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkageRepository extends JpaRepository<VisualizationLinkage, Long>, JpaSpecificationExecutor<VisualizationLinkage> {

    List<VisualizationLinkage> findByDvId(Long dvId);

    @Transactional
    default void deleteByDvId(Long dvId) {
        Specification<VisualizationLinkage> spec = (root, query, cb) ->
                cb.equal(root.get("dvId"), dvId);
        List<VisualizationLinkage> linkages = findAll(spec);
        if (!linkages.isEmpty()) {
            deleteAll(linkages);
        }
    }

    List<VisualizationLinkage> findByDvIdAndSourceViewId(Long dvId, Long sourceViewId);

    @EntityGraph(attributePaths = {"sourceView", "linkageFields"})
    List<VisualizationLinkage> findByDvIdAndLinkageActive(Long dvId, Boolean linkageActive);

}
