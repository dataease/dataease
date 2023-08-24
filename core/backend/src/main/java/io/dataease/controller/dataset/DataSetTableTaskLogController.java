package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.plugins.common.base.domain.DatasetTableTaskLog;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/4 1:32 下午
 */
@Api(tags = "数据集：数据集任务执行记录")
@ApiSupport(order = 100)
@RestController
@RequestMapping("dataset/taskLog")
public class DataSetTableTaskLogController {
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("save")
    public DatasetTableTaskLog save(@RequestBody DatasetTableTaskLog datasetTableTaskLog) {
        return dataSetTableTaskLogService.save(datasetTableTaskLog, true);
    }

    @ApiOperation("分页查询")
    @PostMapping("list/{type}/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskLogDTO>> list(@RequestBody BaseGridRequest request, @PathVariable String type, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableTaskLogService.listTaskLog(request, type));
    }

    @ApiOperation("分页查询")
    @PostMapping("listForDataset/{type}/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskLogDTO>> listForDataset(@RequestBody BaseGridRequest request, @PathVariable String type, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableTaskLogService.listTaskLog(request, type));
    }


    @ApiOperation("导出同步日志")
    @PostMapping("export")
    public void export(@RequestBody BaseGridRequest request) throws Exception{
        dataSetTableTaskLogService.exportExcel(request);
    }

}
