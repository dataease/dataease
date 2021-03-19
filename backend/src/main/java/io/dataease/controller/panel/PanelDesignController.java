package io.dataease.controller.panel;

import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.service.panel.PanelGroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

//    @GetMapping("/find/{id}")
//    public void deleteCircle(@PathVariable String id) {
//        panelGroupService.deleteCircle(id);
//    }

}
