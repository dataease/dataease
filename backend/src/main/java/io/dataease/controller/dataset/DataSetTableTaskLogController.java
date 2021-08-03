package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import io.swagger.annotations.Api;
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

    @ApiOperation("保存")
    @PostMapping("save")
    public DatasetTableTaskLog save(@RequestBody DatasetTableTaskLog datasetTableTaskLog) {
        return dataSetTableTaskLogService.save(datasetTableTaskLog);
    }

    @ApiOperation("删除")
    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableTaskLogService.delete(id);
    }

    @ApiOperation("分页查询")
    @PostMapping("list/{type}/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskLogDTO>> list(@RequestBody BaseGridRequest request, @PathVariable String type, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableTaskLogService.listTaskLog(request, type));
    }

}
