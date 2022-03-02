package io.dataease.controller.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissionProxy;
import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.controller.request.chart.ChartCalRequest;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.controller.response.ChartDetail;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.service.chart.ChartViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/1 1:17 下午
 */
@Api(tags = "视图：视图域")
@ApiSupport(order = 130)
@RestController
@RequestMapping("/chart/view")
public class ChartViewController {
    @Resource
    private ChartViewService chartViewService;

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANNEL_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("/save/{panelId}")
    public ChartViewWithBLOBs save(@PathVariable String panelId, @RequestBody ChartViewWithBLOBs chartViewWithBLOBs) {
        return chartViewService.save(chartViewWithBLOBs);
    }

    @ApiIgnore
    @ApiOperation("查询")
    @PostMapping("/list")
    public List<ChartViewDTO> list(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.list(chartViewRequest);
    }

    @ApiIgnore
    @ApiOperation("查询组")
    @PostMapping("/listAndGroup")
    public List<ChartViewDTO> listAndGroup(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.listAndGroup(chartViewRequest);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANNEL_LEVEL_VIEW, paramIndex = 1)
    @ApiOperation("详细信息")
    @PostMapping("/get/{id}/{panelId}")
    public ChartViewWithBLOBs get(@PathVariable String id, @PathVariable String panelId) {
        return chartViewService.get(id);
    }

    @ApiIgnore
    @ApiOperation("删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        chartViewService.delete(id);
    }

    @DePermissionProxy(value = "proxy", paramIndex = 2)
    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANNEL_LEVEL_VIEW, paramIndex = 1)
    @ApiOperation("数据")
    @PostMapping("/getData/{id}/{panelId}")
    public ChartViewDTO getData(@PathVariable String id, @PathVariable String panelId,
            @RequestBody ChartExtRequest requestList) throws Exception {
        return chartViewService.getData(id, requestList);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANNEL_LEVEL_VIEW, paramIndex = 1)
    @ApiOperation("视图详情")
    @PostMapping("chartDetail/{id}/{panelId}")
    public ChartDetail chartDetail(@PathVariable String id, @PathVariable String panelId) {
        return chartViewService.getChartDetail(id);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANNEL_LEVEL_MANAGE, paramIndex = 1)
    @ApiOperation("复制")
    @PostMapping("chartCopy/{id}/{panelId}")
    public String chartCopy(@PathVariable String id, @PathVariable String panelId) {
        return chartViewService.chartCopy(id, panelId);
    }

    @ApiIgnore
    @GetMapping("searchAdviceSceneId/{panelId}")
    public String searchAdviceSceneId(@PathVariable String panelId) {
        return chartViewService.searchAdviceSceneId(panelId);
    }

    @ApiIgnore
    @ApiOperation("搜索")
    @PostMapping("search")
    public List<ChartViewDTO> search(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.search(chartViewRequest);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANNEL_LEVEL_VIEW)
    @ApiOperation("计算结果")
    @PostMapping("/calcData/{panelId}")
    public ChartViewDTO calcData(@PathVariable String panelId, @RequestBody ChartCalRequest request) throws Exception {
        return chartViewService.calcData(request.getView(), request.getRequestList(), false);
    }

    @ApiIgnore
    @ApiOperation("验证视图是否使用相同数据集")
    @GetMapping("/checkSameDataSet/{viewIdSource}/{viewIdTarget}")
    public String checkSameDataSet(@PathVariable String viewIdSource, @PathVariable String viewIdTarget)
            throws Exception {
        return chartViewService.checkSameDataSet(viewIdSource, viewIdTarget);
    }
}
