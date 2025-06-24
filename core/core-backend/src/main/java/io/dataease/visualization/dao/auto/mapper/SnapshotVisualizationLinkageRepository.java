package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkage;
import org.springframework.data.jpa.repository.*;

import java.util.List;


public interface SnapshotVisualizationLinkageRepository extends JpaRepository<SnapshotVisualizationLinkage, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkage> {

    @EntityGraph(attributePaths = {"linkageFields"})
    List<SnapshotVisualizationLinkage> findByDvIdAndSourceViewId(Long dvId, Long sourceViewId);


    @Modifying
    @Query("DELETE FROM SnapshotVisualizationLinkageField svlf WHERE svlf.linkageId IN " +
            "(SELECT svl.id FROM SnapshotVisualizationLinkage svl WHERE svl.dvId = :dvId " +
            "AND (:sourceViewId IS NULL OR svl.sourceViewId = :sourceViewId))")
    void deleteViewLinkageFieldSnapshot(Long dvId, Long sourceViewId);

    @Modifying
    @Query("DELETE FROM SnapshotVisualizationLinkage svl WHERE svl.dvId = :dvId " +
            "AND (:sourceViewId IS NULL OR svl.sourceViewId = :sourceViewId)")
    void deleteViewLinkageSnapshot(Long dvId,Long sourceViewId);
}
