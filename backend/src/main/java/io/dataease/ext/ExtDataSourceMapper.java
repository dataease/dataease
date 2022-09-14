package io.dataease.ext;

import io.dataease.ext.query.GridExample;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.dto.DatasourceDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtDataSourceMapper {

    List<DatasourceDTO> query(GridExample example);

    List<DatasourceDTO> queryUnion(DatasourceUnionRequest request);

    List<DatasourceDTO> findByPanelId(@Param("panelId") String panelId);




}
