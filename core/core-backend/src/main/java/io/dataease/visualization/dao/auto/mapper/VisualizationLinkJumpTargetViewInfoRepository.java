package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpTargetViewInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;


public interface VisualizationLinkJumpTargetViewInfoRepository extends JpaRepository<VisualizationLinkJumpTargetViewInfo, Long>, JpaSpecificationExecutor<VisualizationLinkJumpTargetViewInfo> {


    @Transactional
    default void updateTargetType(String targetType) {
        Specification<VisualizationLinkJumpTargetViewInfo> spec = (root, query, cb) ->
                cb.equal(root.get("targetType"), targetType);
        VisualizationLinkJumpTargetViewInfo info = findOne(spec).orElse(null);
        if (info != null) {
            info.setTargetType(targetType);
            saveAndFlush(info);
        }
    }

}
