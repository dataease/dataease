package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationLinkJumpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationLinkJumpInfoRepository extends JpaRepository<SnapshotVisualizationLinkJumpInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationLinkJumpInfo> {


    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationLinkJumpInfo c WHERE c.linkJumpId IN :linkJumpIds ")
    void deleteByLinkJumpIds(List<Long> linkJumpIds);

}
