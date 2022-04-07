package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.dto.panel.outerParams.PanelOuterParamsBaseResponse;
import io.dataease.dto.panel.outerParams.PanelOuterParamsDTO;
import io.dataease.service.panel.PanelOuterParamsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Author: wangjiahao
 * Date: 2022/3/17
 * Description:
 */
@Api(tags = "仪表板：外部参数")
@ApiSupport(order = 172)
@RestController
@RequestMapping("outerParams")
public class PanelOuterParamsController {

    @Resource
    private PanelOuterParamsService panelOuterParamsService;

    @ApiOperation("根据仪表板ID获取外部参数信息")
    @GetMapping("/queryWithPanelId/{panelId}")
    public PanelOuterParamsDTO queryWithPanelId(@PathVariable("panelId") String panelId){
       return panelOuterParamsService.queryWithPanelId(panelId) ;
    }

    @ApiOperation("更新外部参数信息")
    @PostMapping("/updateOuterParamsSet")
    public void updateOuterParamsSet(@RequestBody PanelOuterParamsDTO OuterParamsDTO) {
        panelOuterParamsService.updateOuterParamsSet(OuterParamsDTO);
    }

    @ApiOperation("仪表板外部参数映射关系")
    @GetMapping("/getOuterParamsInfo/{panelId}")
    public PanelOuterParamsBaseResponse getOuterParamsInfo(@PathVariable("panelId") String panelId){
       return panelOuterParamsService.getOuterParamsInfo(panelId);
    }
}
