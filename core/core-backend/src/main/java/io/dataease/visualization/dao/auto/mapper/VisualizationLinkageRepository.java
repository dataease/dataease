package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJump;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkageRepository extends JpaRepository<VisualizationLinkage, Long>, JpaSpecificationExecutor<VisualizationLinkage> {

    List<VisualizationLinkage> findByDvId(Long dvId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationLinkage v where v.dvId = :dvId")
    void deleteByDvId(Long dvId);

}
