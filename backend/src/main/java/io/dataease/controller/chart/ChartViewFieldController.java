package io.dataease.controller.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.exception.DEException;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.response.ChartViewField4Type;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.ChartViewField;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.service.chart.ChartViewFieldService;
import io.dataease.service.dataset.DataSetTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @Author gin
 */
@Api(tags = "视图：字段")
@ApiSupport(order = 130)
@RestController
@RequestMapping("/chart/field")
public class ChartViewFieldController {
    @Resource
    private ChartViewFieldService chartViewFieldService;
    @Resource
    private DataSetTableService dataSetTableService;

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("/save/{panelId}")
    public ChartViewField save(@PathVariable String panelId, @RequestBody ChartViewField chartViewField) {
        try {
            // 执行一次sql，确保数据集中所有字段均能正确执行
            DatasetTable datasetTable = dataSetTableService.get(chartViewField.getTableId());
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            BeanUtils.copyProperties(datasetTable, dataSetTableRequest);
            DatasetTableField datasetTableField = new DatasetTableField();
            BeanUtils.copyProperties(chartViewField, datasetTableField);
            dataSetTableService.getPreviewData(dataSetTableRequest, 1, 1, Collections.singletonList(datasetTableField), null);
        } catch (Exception e) {
            DEException.throwException(Translator.get("i18n_calc_field_error"));
        }
        return chartViewFieldService.save(chartViewField);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_MANAGE, paramIndex = 1)
    @ApiOperation("删除")
    @PostMapping("/delete/{id}/{panelId}")
    public void delete(@PathVariable String id, @PathVariable String panelId) {
        chartViewFieldService.delete(id);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_MANAGE, paramIndex = 1)
    @ApiOperation("删除视图的字段")
    @PostMapping("/deleteByChartId/{chartId}/{panelId}")
    public void deleteByChartId(@PathVariable String chartId, @PathVariable String panelId) {
        chartViewFieldService.deleteByChartId(chartId);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_VIEW, paramIndex = 1)
    @ApiOperation("分组查询表下属字段")
    @PostMapping("listByDQ/{chartId}/{panelId}")
    public ChartViewField4Type listByDQ(@PathVariable String chartId, @PathVariable String panelId) {
        ChartViewField chartViewField = new ChartViewField();
        chartViewField.setChartId(chartId);
        chartViewField.setGroupType("d");
        List<ChartViewField> dimensionList = chartViewFieldService.list(chartViewField);
        chartViewField.setGroupType("q");
        List<ChartViewField> quotaList = chartViewFieldService.list(chartViewField);

        ChartViewField4Type chartViewField4Type = new ChartViewField4Type();
        chartViewField4Type.setDimensionList(dimensionList);
        chartViewField4Type.setQuotaList(quotaList);
        return chartViewField4Type;
    }
}
