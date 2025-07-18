package io.dataease.api.dataset;

import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface DataAssistantApi {
    @GetMapping("/datasource")
    List<DataSQLBotAssistantVO> getDatasourceList();
}
