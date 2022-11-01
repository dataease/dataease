package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.plugins.common.base.domain.DatasetTableTask;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import io.dataease.service.dataset.DataSetTableTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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

    @DePermission(type = DePermissionType.DATASET, value = "datasetTableTask.tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
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

    @ApiOperation("批量删除")
    @PostMapping("/batchDelete")
    public void batchDelete(@RequestBody List<String> ids) {
        dataSetTableTaskService.batchDelete(ids);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("查询")
    @PostMapping("list")
    public List<DataSetTaskDTO> list(@RequestBody DatasetTableTask datasetTableTask) {
        return dataSetTableTaskService.list(datasetTableTask);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("分页查询")
    @PostMapping("list/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskDTO>> list(@RequestBody DatasetTableTask datasetTableTask, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<DataSetTaskDTO>> listPager = PageUtils.setPageInfo(page, dataSetTableTaskService.list(datasetTableTask));
        List<DataSetTaskDTO> listObject = listPager.getListObject();
        for (DataSetTaskDTO dto : listObject) {
            dataSetTableTaskLogService.lastExecStatus(dto);
        }
        return listPager;
    }

    @ApiOperation("分页查询")
    @PostMapping("/pageList/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskDTO>> taskList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);

        Pager<List<DataSetTaskDTO>> listPager = PageUtils.setPageInfo(page, dataSetTableTaskService.taskList4User(request));
        List<DataSetTaskDTO> listObject = listPager.getListObject();
        for (DataSetTaskDTO dto : listObject) {
            dataSetTableTaskLogService.lastExecStatus(dto);
        }

        return listPager;
    }

    @ApiIgnore
    @PostMapping("/lastExecStatus")
    public DataSetTaskDTO lastExecStatus(@RequestBody DataSetTaskDTO datasetTableTask) {
        return dataSetTableTaskLogService.lastExecStatus(datasetTableTask);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("更新状态")
    @PostMapping("/updateStatus")
    public void updateStatus(@RequestBody DatasetTableTask datasetTableTask) throws Exception {
        dataSetTableTaskService.updateDatasetTableTaskStatus(datasetTableTask);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("执行任务")
    @PostMapping("/execTask")
    public void execTask(@RequestBody DatasetTableTask datasetTableTask) throws Exception {
        dataSetTableTaskService.execTask(datasetTableTask);
    }


    @ApiOperation("详情")
    @PostMapping("detail/{id}")
    public DataSetTaskDTO detail(@PathVariable("id") String id) {
        return dataSetTableTaskService.detail(id);
    }

}
