package io.dataease.api.spreadsheet;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.spreadsheet.dto.PluginFieldValuesRequest;
import io.dataease.api.spreadsheet.dto.PluginQueryRequest;
import io.dataease.api.spreadsheet.vo.PluginQueryResponse;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.SPREADSHEET;

@Tag(name = "电子表格数据管理")
@ApiSupport(order = 969)
@DeApiPath(value = "/spreadsheetData", rt = SPREADSHEET)
public interface SpreadsheetDataApi {

    @Operation(summary = "查询电子表格查询数量上限")
    @GetMapping("/queryLimit")
    Long queryLimit();

    @Operation(summary = "插件获取数据")
    @PostMapping("/queryData")
    PluginQueryResponse queryData(@RequestBody PluginQueryRequest request);

    @Operation(summary = "查询电子表格字段候选值")
    @PostMapping("/queryFieldValues")
    List<String> queryFieldValues(@RequestBody PluginFieldValuesRequest request);
}
