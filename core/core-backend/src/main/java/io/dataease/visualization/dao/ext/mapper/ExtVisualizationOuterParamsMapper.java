package io.dataease.visualization.dao.ext.mapper;


import io.dataease.api.dataset.vo.CoreDatasetGroupVO;
import io.dataease.api.visualization.dto.VisualizationOuterParamsDTO;
import io.dataease.api.visualization.dto.VisualizationOuterParamsInfoDTO;
import io.dataease.visualization.dao.auto.entity.VisualizationOuterParamsInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtVisualizationOuterParamsMapper {

    VisualizationOuterParamsDTO queryWithVisualizationId(@Param("visualizationId") String visualizationId);

    void deleteOuterParamsTargetWithVisualizationId(@Param("visualizationId") String visualizationId);

    void deleteOuterParamsInfoWithVisualizationId(@Param("visualizationId") String visualizationId);

    void deleteOuterParamsWithVisualizationId(@Param("visualizationId") String visualizationId);

    void deleteOuterParamsTargetWithVisualizationIdSnapshot(@Param("visualizationId") Long visualizationId);

    void deleteOuterParamsInfoWithVisualizationIdSnapshot(@Param("visualizationId") Long visualizationId);

    void deleteOuterParamsWithVisualizationIdSnapshot(@Param("visualizationId") Long visualizationId);

    List<VisualizationOuterParamsInfoDTO> getVisualizationOuterParamsInfo(@Param("visualizationId") String visualizationId);

    List<VisualizationOuterParamsInfo> getVisualizationOuterParamsInfoBase(@Param("visualizationId") String visualizationId);

    List<CoreDatasetGroupVO> queryDsWithVisualizationId(@Param("visualizationId") String visualizationId);
}
