package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.PanelGroup;
import io.dataease.base.domain.PanelGroupWithBLOBs;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.service.panel.PanelGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Api(tags = "仪表板：仪表板组")
@ApiSupport(order = 150)
@RestController
@RequestMapping("panel/group")
public class PanelGroupController {

    @Resource
    private PanelGroupService panelGroupService;

    @ApiOperation("查询树")
    @PostMapping("/tree")
    public List<PanelGroupDTO> tree(@RequestBody PanelGroupRequest request) {
        return panelGroupService.tree(request);
    }

    @ApiOperation("默认树")
    @PostMapping("/defaultTree")
    public List<PanelGroupDTO> defaultTree(@RequestBody PanelGroupRequest request) {
        return panelGroupService.defaultTree(request);
    }

    @ApiOperation("保存")
    @PostMapping("/save")
    @I18n
    public PanelGroup saveOrUpdate(@RequestBody PanelGroupRequest request) {
        return panelGroupService.saveOrUpdate(request);
    }

    @ApiOperation("删除")
    @PostMapping("/deleteCircle/{id}")
    public void deleteCircle(@PathVariable String id) {
        panelGroupService.deleteCircle(id);
    }

    @ApiOperation("详息")
    @GetMapping("/findOne/{id}")
    public PanelGroupWithBLOBs findOne(@PathVariable String id) throws Exception {
        return panelGroupService.findOne(id);
    }


}
