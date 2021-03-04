package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author gin
 * @Date 2021/3/4 1:32 下午
 */
@RestController
@RequestMapping("dataset/taskLog")
public class DataSetTableTaskLogController {
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;

    @PostMapping("save")
    public DatasetTableTaskLog save(@RequestBody DatasetTableTaskLog datasetTableTaskLog) {
        return dataSetTableTaskLogService.save(datasetTableTaskLog);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableTaskLogService.delete(id);
    }

}
