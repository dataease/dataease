package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkJumpRepository extends JpaRepository<VisualizationLinkJump, Long>, JpaSpecificationExecutor<VisualizationLinkJump> {

    List<VisualizationLinkJump> findBySourceDvId(Long sourceDvId);


    @Transactional
    default void deleteBySourceDvId(Long sourceDvId) {
        List<VisualizationLinkJump> jumps = findBySourceDvId(sourceDvId);
        if (jumps != null && !jumps.isEmpty()) {
            deleteAll(jumps);
        }
    }

}
