package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkJumpInfoRepository extends JpaRepository<VisualizationLinkJumpInfo, Long>, JpaSpecificationExecutor<VisualizationLinkJumpInfo> {


    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationLinkJumpInfo c WHERE c.linkJumpId IN :linkJumpIds ")
    void deleteByLinkJumpIds(List<Long> linkJumpIds);

    @Query("SELECT i FROM VisualizationLinkJumpInfo i WHERE i.linkType = 'outer' AND i.linkJump.sourceDvId = :dvId")
    List<VisualizationLinkJumpInfo> findOuterLinkJumpInfoByDvId(Long dvId);
}
