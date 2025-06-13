package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJump;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface VisualizationLinkJumpRepository extends JpaRepository<VisualizationLinkJump, Long>, JpaSpecificationExecutor<VisualizationLinkJump> {

    List<VisualizationLinkJump> findBySourceDvId(Long sourceDvId);

}
