package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.service.dataset.DataSetTableService;
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
    @Resource
    private DataSetTableService dataSetTableService;

    @PostMapping("save")
    public DatasetTableTask save(@RequestBody DataSetTaskRequest dataSetTaskRequest) throws Exception {
        dataSetTableService.saveIncrementalConfig(dataSetTaskRequest.getDatasetTableIncrementalConfig());
        return dataSetTableTaskService.save(dataSetTaskRequest.getDatasetTableTask());
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
