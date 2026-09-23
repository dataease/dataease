package io.dataease.api.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.chart.request.ChartCalcFieldValidateRequest;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.PANEL;

/**
 * @Author Junjun
 */
@Tag(name = "图表管理:数据")
@ApiSupport(order = 989)
@DeApiPath(value = "/chartData", rt = PANEL)
public interface ChartDataApi {
    @Operation(summary = "获取图表数据")
    @PostMapping("getData")
    ChartViewDTO getData(@RequestBody ChartViewDTO chartViewDTO) throws Exception;

    @Operation(summary = "校验图表计算字段")
    @PostMapping("validateCalcField")
    void validateCalcField(@RequestBody ChartCalcFieldValidateRequest request) throws Exception;

    @Operation(summary = "导出数据")
    @PostMapping("/innerExportDetails")
    @DePermit(value = {"#p0.dvId+':export_view'"}, busiFlag = "#p0.busiFlag")
    void innerExportDetails(@RequestBody ChartExcelRequest request, HttpServletResponse response) throws Exception;

    @Operation(summary = "导出明细数据")
    @PostMapping("/innerExportDataSetDetails")
    @DePermit(value = {"#p0.dvId+':export_detail'"}, busiFlag = "#p0.busiFlag")
    void innerExportDataSetDetails(@RequestBody ChartExcelRequest request, HttpServletResponse response) throws Exception;

    @Operation(summary = "获取字段值")
    @PostMapping("/getFieldData/{fieldId}/{fieldType}")
    List<String> getFieldData(@RequestBody ChartViewDTO view, @PathVariable Long fieldId, @PathVariable String fieldType) throws Exception;

    @Operation(summary = "获取下钻字段值")
    @PostMapping("/getDrillFieldData/{fieldId}")
    List<String> getDrillFieldData(@RequestBody ChartViewDTO view, @PathVariable Long fieldId) throws Exception;
}
