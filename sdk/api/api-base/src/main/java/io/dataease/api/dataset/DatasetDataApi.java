package io.dataease.api.dataset;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.auth.DePermit;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
public interface DatasetDataApi {
    @DePermit({"m:read"})
    @PostMapping("previewData")
    Map<String, Object> previewData(@RequestBody DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception;

    @DePermit({"m:read"})
    @PostMapping("tableField")
    List<DatasetTableFieldDTO> tableField(@RequestBody DatasetTableDTO datasetTableDTO) throws Exception;

    @PostMapping("previewSql")
    Map<String, Object> previewSql(@RequestBody PreviewSqlDTO dto) throws Exception;

    @PostMapping("previewSqlCheck")
    Map<String, Object> previewSqlCheck(@RequestBody PreviewSqlDTO dto) throws Exception;

    @PostMapping("enumValue")
    List<String> getFieldEnum(@RequestBody List<Long> ids) throws Exception;
}
