package io.dataease.api.dataset;

import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DataAssistantApi {
    // V3 暂时禁用 SQLBot，保留实现但不注册 HTTP 路由。
    // @GetMapping("/datasource")
    List<DataSQLBotAssistantVO> getDatasourceList(@RequestParam(required = false) Long dsId, @RequestParam(required = false) Long tableId);
}
