package io.dataease.controller.exportCenter;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;

import io.dataease.controller.request.dataset.DataSetExportRequest;

import io.dataease.plugins.common.base.domain.ExportTask;

import io.dataease.service.exportCenter.ExportCenterService;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.List;

@ApiSupport(order = 31)
@RequestMapping("exportCenter")
@RestController
public class ExportCenterController {

    @Resource
    private ExportCenterService exportCenterService;



    @PostMapping("/exportTasks/{status}")
    public List<ExportTask> exportTasks(@PathVariable String status) {
        return exportCenterService.exportTasks(status);
    }


    @PostMapping("/exportTasks/{status}")
    public void  addTask(String exportFrom, String exportFromType, DataSetExportRequest request){

    }

}
