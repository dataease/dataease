package io.dataease.base.mapper.ext;

import io.dataease.dto.dataset.DataSetTableUnionDTO;

import java.util.List;

public interface ExtDatasetTableUnionMapper {
    List<DataSetTableUnionDTO> selectBySourceTableId(String tableId);

    List<DataSetTableUnionDTO> selectByTargetTableId(String tableId);
}