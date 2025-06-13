package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface SnapshotDataVisualizationInfoRepository extends JpaRepository<SnapshotDataVisualizationInfo, Long>, JpaSpecificationExecutor<SnapshotDataVisualizationInfo> {
    List<SnapshotDataVisualizationInfo> findBySceneId(Long sceneId);

}
