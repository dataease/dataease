package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.service.dataset.DataSetTableTaskLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("list/{type}/{goPage}/{pageSize}")
    public Pager<List<DataSetTaskLogDTO>> list(@RequestBody BaseGridRequest request, @PathVariable String type, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableTaskLogService.listTaskLog(request, type));
    }

}
