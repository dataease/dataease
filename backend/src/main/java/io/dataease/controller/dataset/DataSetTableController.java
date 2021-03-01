package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.service.dataset.DataSetTableService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    public void batchAdd(@RequestBody List<DatasetTable> datasetTable) throws Exception {
        dataSetTableService.batchInsert(datasetTable);
    }

    @PostMapping("update")
    public DatasetTable save(@RequestBody DatasetTable datasetTable) throws Exception {
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

    @PostMapping("getFields")
    public List<TableFiled> getFields(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getFields(dataSetTableRequest);
    }

    @PostMapping("getFieldsFromDE")
    public Map<String, List<DatasetTableField>> getFieldsFromDE(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getFieldsFromDE(dataSetTableRequest);
    }

    @PostMapping("getData")
    public List<String[]> getData(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getData(dataSetTableRequest);
    }

    @PostMapping("getPreviewData")
    public Map<String, Object> getPreviewData(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getPreviewData(dataSetTableRequest);
    }
}
