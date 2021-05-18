package io.dataease.controller.panel;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.base.domain.PanelGroupWithBLOBs;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.service.CommonFilesService;
import io.dataease.service.panel.PanelGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@RestController
@RequestMapping("panel/group")
public class PanelGroupController {

    @Resource
    private PanelGroupService panelGroupService;

    @PostMapping("/tree")
    public List<PanelGroupDTO> tree(@RequestBody PanelGroupRequest request) {
        request.setLevel(0);
        return panelGroupService.tree(request);
    }

    @PostMapping("/defaultTree")
    public List<PanelGroupDTO> defaultTree(@RequestBody PanelGroupRequest request) {
        return panelGroupService.getDefaultTree(request);
    }

    @PostMapping("/save")
    public PanelGroupDTO save(@RequestBody PanelGroupRequest request) {
        return panelGroupService.save(request);
    }

    @PostMapping("/deleteCircle/{id}")
    public void deleteCircle(@PathVariable String id) {
        panelGroupService.deleteCircle(id);
    }

    @GetMapping("/findOne/{id}")
    public PanelGroupWithBLOBs findOne(@PathVariable String id) throws Exception {
        return panelGroupService.findOne(id);
    }


}
