package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJump;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkJumpRepository extends JpaRepository<VisualizationLinkJump, Long>, JpaSpecificationExecutor<VisualizationLinkJump> {

    List<VisualizationLinkJump> findBySourceDvId(Long sourceDvId);



    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationLinkJump c WHERE c.sourceDvId = :sourceDvId ")
    void deleteBySourceDvId(Long sourceDvId);
}
