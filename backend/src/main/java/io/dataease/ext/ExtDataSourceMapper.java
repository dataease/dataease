package io.dataease.ext;

import io.dataease.ext.query.GridExample;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.dto.DatasourceDTO;

import java.util.List;

public interface ExtDataSourceMapper {

    List<DatasourceDTO> query(GridExample example);

    List<DatasourceDTO> queryUnion(DatasourceUnionRequest request);



}
