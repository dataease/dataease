package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTable;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.service.dataset.DataSetTableService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/20 8:29 下午
 */
@RestController
@RequestMapping("dataset/table")
public class DataSetTableController {
    @Resource
    private DataSetTableService dataSetTableService;

    @PostMapping("batchAdd")
    public void batchAdd(@RequestBody List<DatasetTable> datasetTable) {
        dataSetTableService.batchInsert(datasetTable);
    }

    @PostMapping("update")
    public DatasetTable save(@RequestBody DatasetTable datasetTable) {
        return dataSetTableService.save(datasetTable);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableService.delete(id);
    }

    @PostMapping("list")
    public List<DatasetTable> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.list(dataSetTableRequest);
    }

    @PostMapping("get/{id}")
    public DatasetTable get(@PathVariable String id) {
        return dataSetTableService.get(id);
    }
}
