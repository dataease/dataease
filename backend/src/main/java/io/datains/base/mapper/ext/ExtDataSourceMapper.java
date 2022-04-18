package io.datains.base.mapper.ext;

import io.datains.base.mapper.ext.query.GridExample;
import io.datains.controller.request.DatasourceUnionRequest;
import io.datains.dto.DatasourceDTO;

import java.util.List;

public interface ExtDataSourceMapper {

    List<DatasourceDTO> query(GridExample example);

    List<DatasourceDTO> queryUnion(DatasourceUnionRequest request);



}
