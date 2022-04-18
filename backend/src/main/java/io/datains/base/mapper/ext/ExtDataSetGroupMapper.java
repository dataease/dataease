package io.datains.base.mapper.ext;

import io.datains.controller.request.dataset.DataSetGroupRequest;
import io.datains.dto.dataset.DataSetGroupDTO;

import java.util.List;
import java.util.Map;

public interface ExtDataSetGroupMapper {
    List<DataSetGroupDTO> search(DataSetGroupRequest ChartGroup);

    Map<String, String> searchIds(String id, String type);
}
