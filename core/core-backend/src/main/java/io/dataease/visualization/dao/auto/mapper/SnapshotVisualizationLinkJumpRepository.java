package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationLinkJumpRepository extends JpaRepository<SnapshotVisualizationLinkJump, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkJump> {


    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationLinkJump c WHERE c.sourceDvId = :sourceDvId ")
    void deleteBySourceDvId(Long sourceDvId);

}
