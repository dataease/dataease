package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationOuterParamsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationOuterParamsInfoRepository extends JpaRepository<VisualizationOuterParamsInfo, Long>, JpaSpecificationExecutor<VisualizationOuterParamsInfo> {


    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationOuterParamsInfo c WHERE c.paramsId in :paramsIds ")
    void deleteByParamsIds(List<String> paramsIds);
}
