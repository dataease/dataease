package io.dataease.controller.panel;

import io.dataease.service.panel.PanelGroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@RestController
@RequestMapping("panel/design")
public class PanelDesignController {

    @Resource
    private PanelGroupService panelGroupService;

    @PostMapping("/saveDesign/{id}")
    public void deleteCircle(@PathVariable String id) {
        panelGroupService.deleteCircle(id);
    }


}
