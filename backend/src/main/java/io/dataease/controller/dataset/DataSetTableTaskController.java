package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.DatasetTableTask;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import io.dataease.service.dataset.DataSetTableTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/4 1:32 下午
 */
@Api(tags = "数据集：数据集任务")
@ApiSupport(order = 90)
@RestController
@RequestMapping("dataset/task")
public class DataSetTableTaskController {
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;

    @ApiOperation("保存")
    @PostMapping("save")
    public DatasetTableTask save(@RequestBody DataSetTaskRequest dataSetTaskRequest) throws Exception {
        return dataSetTableTaskService.save(dataSetTaskRequest);
    }

    @ApiOperation("删除")
    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableTaskService.delete(id);
    }

    @ApiOperation("查询")
    @PostMapping("list")
    public List<DatasetTableTask> list(@RequestBody DatasetTableTask datasetTableTask) {
        return dataSetTableTaskService.list(datasetTableTask);
    }

    @ApiOperation("分页查询")
    @PostMapping("/pageList/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskDTO>> taskList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);

        return PageUtils.setPageInfo(page, dataSetTableTaskService.taskList4User(request));
    }

    @ApiOperation("上次执行时间")
    @PostMapping("/lastExecStatus")
    public DataSetTaskDTO lastExecStatus(@RequestBody DataSetTaskDTO datasetTableTask) {
        return dataSetTableTaskLogService.lastExecStatus(datasetTableTask);
    }

    @ApiOperation("更新状态")
    @PostMapping("/updateStatus")
    public void updateStatus(@RequestBody DatasetTableTask datasetTableTask) throws Exception{
        dataSetTableTaskService.updateDatasetTableTaskStatus(datasetTableTask);
    }

    @ApiOperation("执行任务")
    @PostMapping("/execTask")
    public void execTask(@RequestBody DatasetTableTask datasetTableTask) throws Exception{
        dataSetTableTaskService.execTask(datasetTableTask);
    }

}
