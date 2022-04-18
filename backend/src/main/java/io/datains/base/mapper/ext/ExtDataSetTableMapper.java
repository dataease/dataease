package io.datains.base.mapper.ext;

import io.datains.controller.request.dataset.DataSetTableRequest;
import io.datains.dto.dataset.DataSetTableDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtDataSetTableMapper {
    List<DataSetTableDTO> search(DataSetTableRequest request);

    DataSetTableDTO searchOne(DataSetTableRequest request);

    List<DataSetTableDTO> searchDataSetTableWithPanelId(@Param("panelId") String panelId, @Param("userId") String userId);

}
