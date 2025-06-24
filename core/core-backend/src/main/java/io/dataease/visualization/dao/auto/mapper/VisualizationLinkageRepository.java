package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkageRepository extends JpaRepository<VisualizationLinkage, Long>, JpaSpecificationExecutor<VisualizationLinkage> {

    List<VisualizationLinkage> findByDvId(Long dvId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationLinkage v where v.dvId = :dvId")
    void deleteByDvId(Long dvId);

    List<VisualizationLinkage> findByDvIdAndSourceViewId(Long dvId, Long sourceViewId);

    @EntityGraph(attributePaths = {"sourceView", "linkageFields"})
    List<VisualizationLinkage> findByDvIdAndLinkageActive(Long dvId, Boolean linkageActive);

    @Modifying
    @Query("DELETE FROM VisualizationLinkageField vlf WHERE vlf.linkageId IN " +
            "(SELECT vl.id FROM VisualizationLinkage vl WHERE vl.dvId = :dvId " +
            "AND (:sourceViewId IS NULL OR vl.sourceViewId = :sourceViewId))")
    void deleteViewLinkageField(Long dvId, Long sourceViewId);
}
