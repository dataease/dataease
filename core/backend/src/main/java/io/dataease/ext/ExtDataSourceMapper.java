package io.dataease.ext;

import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.RelationDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtDataSourceMapper {


    List<DatasourceDTO> queryUnion(DatasourceUnionRequest request);

    List<DatasourceDTO> findByPanelId(@Param("panelId") String panelId);

    List<DatasourceDTO> findByTableIds(@Param("tableIds") List<String> tableIds);

    DatasourceDTO queryDetails(@Param("datasourceId") String datasourceId, @Param("userId") String userId);

    List<RelationDTO> queryDatasourceRelation(@Param("datasourceId") String datasourceId, @Param("userId") Long userId);
}
