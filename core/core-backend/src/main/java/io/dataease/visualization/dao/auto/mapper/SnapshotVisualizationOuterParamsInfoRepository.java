package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParamsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationOuterParamsInfoRepository extends JpaRepository<SnapshotVisualizationOuterParamsInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParamsInfo> {

    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationOuterParamsInfo c WHERE c.paramsId IN :paramsIds ")
    void deleteByParamsIds(List<String> paramsIds);

}
