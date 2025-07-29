package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface SnapshotVisualizationLinkageRepository extends JpaRepository<SnapshotVisualizationLinkage, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkage> {

    @EntityGraph(attributePaths = {"linkageFields"})
    List<SnapshotVisualizationLinkage> findByDvIdAndSourceViewId(Long dvId, Long sourceViewId);

}
