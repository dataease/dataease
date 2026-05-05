package io.dataease.api.spreadsheet;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.spreadsheet.dto.PluginQueryRequest;
import io.dataease.api.spreadsheet.vo.PluginQueryResponse;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static io.dataease.constant.AuthResourceEnum.SPREADSHEET;

@Tag(name = "电子表格数据管理")
@ApiSupport(order = 969)
@DeApiPath(value = "/spreadsheetData", rt = SPREADSHEET)
public interface SpreadsheetDataApi {

    @Operation(summary = "插件获取数据")
    @PostMapping("/queryData")
    PluginQueryResponse queryData(@RequestBody PluginQueryRequest request);
}
