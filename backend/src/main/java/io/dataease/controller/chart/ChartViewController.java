package io.dataease.controller.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.service.chart.ChartViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @ApiOperation("保存")
    @PostMapping("/save")
    public ChartViewWithBLOBs save(@RequestBody ChartViewWithBLOBs chartViewWithBLOBs) {
        return chartViewService.save(chartViewWithBLOBs);
    }

    @ApiOperation("查询")
    @PostMapping("/list")
    public List<ChartViewDTO> list(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.list(chartViewRequest);
    }

    @ApiOperation("查询组")
    @PostMapping("/listAndGroup")
    public List<ChartViewDTO> listAndGroup(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.listAndGroup(chartViewRequest);
    }

    @ApiOperation("详息")
    @PostMapping("/get/{id}")
    public ChartViewWithBLOBs get(@PathVariable String id) {
        return chartViewService.get(id);
    }

    @ApiOperation("删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        chartViewService.delete(id);
    }

    @ApiOperation("数据")
    @PostMapping("/getData/{id}")
    public ChartViewDTO getData(@PathVariable String id, @RequestBody ChartExtRequest requestList) throws Exception {
        return chartViewService.getData(id, requestList);
    }

    @ApiOperation("视图详情")
    @PostMapping("chartDetail/{id}")
    public Map<String, Object> chartDetail(@PathVariable String id) {
        return chartViewService.getChartDetail(id);
    }

    @ApiOperation("复制")
    @PostMapping("chartCopy/{id}")
    public String chartCopy(@PathVariable String id) {
        return chartViewService.chartCopy(id);
    }

    @ApiIgnore
    @GetMapping("searchAdviceSceneId/{panelId}")
    public String searchAdviceSceneId(@PathVariable String panelId) {
        return chartViewService.searchAdviceSceneId(panelId);
    }

    @ApiOperation("根据权限查详情")
    @PostMapping("/getOneWithPermission/{id}")
    public ChartViewDTO getOneWithPermission(@PathVariable String id, @RequestBody ChartExtRequest requestList) throws Exception {
        //如果能获取用户 则添加对应的权限
        ChartViewDTO dto = chartViewService.getData(id, requestList);
        if (dto != null && AuthUtils.getUser() != null) {
            ChartViewDTO permissionDto = chartViewService.getOneWithPermission(dto.getId());
            dto.setPrivileges(permissionDto.getPrivileges());
        }
        return dto;
    }

    @ApiOperation("搜索")
    @PostMapping("search")
    public List<ChartViewDTO> search(@RequestBody ChartViewRequest chartViewRequest) {
        return chartViewService.search(chartViewRequest);
    }
}
