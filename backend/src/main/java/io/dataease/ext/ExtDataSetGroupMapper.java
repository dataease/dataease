package io.dataease.ext;

import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;

import java.util.List;
import java.util.Map;

public interface ExtDataSetGroupMapper {
    List<DataSetGroupDTO> search(DataSetGroupRequest ChartGroup);

    Map<String, String> searchIds(String id, String type);
}
