package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJumpTargetViewInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationLinkJumpTargetViewInfoRepository extends JpaRepository<SnapshotVisualizationLinkJumpTargetViewInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkJumpTargetViewInfo> {



    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationLinkJumpTargetViewInfo c WHERE c.linkJumpInfoId IN :linkJumpIds ")
    void deleteByLinkJumpInfoIds(List<Long> linkJumpIds);
}
