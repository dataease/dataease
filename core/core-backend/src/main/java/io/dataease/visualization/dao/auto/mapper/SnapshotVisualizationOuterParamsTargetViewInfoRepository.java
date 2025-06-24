package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotVisualizationOuterParamsTargetViewInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SnapshotVisualizationOuterParamsTargetViewInfoRepository extends JpaRepository<SnapshotVisualizationOuterParamsTargetViewInfo, Long>, JpaSpecificationExecutor<SnapshotVisualizationOuterParamsTargetViewInfo> {


    @Modifying
    @Transactional
    @Query("DELETE FROM SnapshotVisualizationOuterParamsTargetViewInfo c WHERE c.paramsInfoId IN :paramsInfoIds ")
    void deleteByParamsInfoIds(List<String> paramsInfoIds);

}
