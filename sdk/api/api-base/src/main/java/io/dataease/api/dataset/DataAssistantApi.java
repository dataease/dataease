package io.dataease.api.dataset;

import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import io.dataease.api.dataset.vo.DataSQLBotDatasetVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DataAssistantApi {
    @GetMapping("/datasource")
    List<DataSQLBotAssistantVO> getDatasourceList(@RequestParam(required = false) Long dsId, @RequestParam(required = false) Long tableId);

    @GetMapping("/dataset/{dvInfo}")
    List<DataSQLBotDatasetVO> getDatasetList(@PathVariable String dvInfo);
}
