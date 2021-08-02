package io.dataease.controller.chart;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.ChartGroup;
import io.dataease.controller.request.chart.ChartGroupRequest;
import io.dataease.dto.chart.ChartGroupDTO;
import io.dataease.service.chart.ChartGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "视图：视图组")
@ApiSupport(order = 120)
@RestController
@RequestMapping("chart/group")
public class ChartGroupController {
    @Resource
    private ChartGroupService chartGroupService;

    @ApiOperation("保存")
    @PostMapping("/save")
    public ChartGroupDTO save(@RequestBody ChartGroup ChartGroup) {
        return chartGroupService.save(ChartGroup);
    }

    @ApiOperation("查询树")
    @PostMapping("/tree")
    public List<ChartGroupDTO> tree(@RequestBody ChartGroupRequest ChartGroup) {
        return chartGroupService.tree(ChartGroup);
    }

    @ApiOperation("查询树节点")
    @PostMapping("/treeNode")
    public List<ChartGroupDTO> treeNode(@RequestBody ChartGroupRequest ChartGroup) {
        return chartGroupService.tree(ChartGroup);
    }

    @ApiOperation("删除")
    @PostMapping("/delete/{id}")
    public void tree(@PathVariable String id) {
        chartGroupService.delete(id);
    }

    @ApiIgnore
    @PostMapping("/getScene/{id}")
    public ChartGroup getScene(@PathVariable String id) {
        return chartGroupService.getScene(id);
    }
}
