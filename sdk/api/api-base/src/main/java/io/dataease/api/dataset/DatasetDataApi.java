package io.dataease.api.dataset;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author Junjun
 */
public interface DatasetDataApi {
    @PostMapping("previewData")
    Map<String, Object> previewData(@RequestBody DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception;

    @PostMapping("tableField")
    List<DatasetTableFieldDTO> tableField(@RequestBody DatasetTableDTO datasetTableDTO) throws Exception;

    @PostMapping("previewSql")
    Map<String, Object> previewSql(@RequestBody PreviewSqlDTO dto) throws Exception;

    @PostMapping("enumValue")
    Set<String> getFieldEnum(@RequestBody List<String> ids) throws Exception;
}
