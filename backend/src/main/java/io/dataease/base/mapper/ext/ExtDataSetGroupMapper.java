package io.dataease.base.mapper.ext;

import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;

import java.util.List;

public interface ExtDataSetGroupMapper {
    List<DataSetGroupDTO> search(DataSetGroupRequest ChartGroup);
}
