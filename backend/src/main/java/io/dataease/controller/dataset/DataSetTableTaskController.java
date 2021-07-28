package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.DatasetTableTask;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import io.dataease.service.dataset.DataSetTableTaskService;
import io.swagger.annotations.ApiOperation;
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
    private DataSetTableTaskLogService dataSetTableTaskLogService;

    @PostMapping("save")
    public DatasetTableTask save(@RequestBody DataSetTaskRequest dataSetTaskRequest) throws Exception {
        return dataSetTableTaskService.save(dataSetTaskRequest);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableTaskService.delete(id);
    }

    @PostMapping("list")
    public List<DatasetTableTask> list(@RequestBody DatasetTableTask datasetTableTask) {
        return dataSetTableTaskService.list(datasetTableTask);
    }

    @ApiOperation("查看数据集任务")
    @PostMapping("/pageList/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskDTO>> taskList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);

        return PageUtils.setPageInfo(page, dataSetTableTaskService.taskList(request));
    }

    @PostMapping("/lastExecStatus")
    public DataSetTaskDTO lastExecStatus(@RequestBody DataSetTaskDTO datasetTableTask) {
        return dataSetTableTaskLogService.lastExecStatus(datasetTableTask);
    }

    @PostMapping("/updateStatus")
    public void updateStatus(@RequestBody DatasetTableTask datasetTableTask) {
        dataSetTableTaskService.updateDatasetTableTaskStatus(datasetTableTask);
    }

    @PostMapping("/execTask")
    public void execTask(@RequestBody DatasetTableTask datasetTableTask) throws Exception{
        dataSetTableTaskService.execTask(datasetTableTask);
    }

}
