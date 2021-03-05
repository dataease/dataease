package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.service.dataset.DataSetTableTaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/4 1:32 下午
 */
@RestController
@RequestMapping("dataset/task")
public class DataSetTableTaskController {
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;

    @PostMapping("save")
    public DatasetTableTask save(@RequestBody DatasetTableTask datasetTableTask) throws Exception {
        return dataSetTableTaskService.save(datasetTableTask);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableTaskService.delete(id);
    }

    @PostMapping("list")
    public List<DatasetTableTask> list(@RequestBody DatasetTableTask datasetTableTask) {
        return dataSetTableTaskService.list(datasetTableTask);
    }
}
