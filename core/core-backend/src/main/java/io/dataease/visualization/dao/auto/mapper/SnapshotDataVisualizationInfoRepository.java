package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface SnapshotDataVisualizationInfoRepository extends JpaRepository<SnapshotDataVisualizationInfo, Long>, JpaSpecificationExecutor<SnapshotDataVisualizationInfo> {

    @Query("SELECT s FROM SnapshotDataVisualizationInfo s " +
            "WHERE s.deleteFlag = false AND s.id = :dvId " +
            "AND (:dvType IS NULL OR s.type = :dvType)")
    Optional<SnapshotDataVisualizationInfo> findSnapshotDvInfoEntity(Long dvId, String dvType);

}
