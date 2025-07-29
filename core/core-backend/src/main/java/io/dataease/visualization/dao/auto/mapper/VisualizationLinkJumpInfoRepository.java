package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkJumpInfoRepository extends JpaRepository<VisualizationLinkJumpInfo, Long>, JpaSpecificationExecutor<VisualizationLinkJumpInfo> {


    @Transactional
    default void deleteByLinkJumpIds(List<Long> linkJumpIds) {
        Specification<VisualizationLinkJumpInfo> spec = (root, query, cb) ->
                root.get("linkJumpId").in(linkJumpIds);
        List<VisualizationLinkJumpInfo> infos = findAll(spec);
        if (!infos.isEmpty()) {
            deleteAll(infos);
        }
    }

}
