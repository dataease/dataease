package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableField;
import io.dataease.service.dataset.DataSetFieldService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author gin
 * @Date 2021/2/24 4:28 下午
 */
@RestController
@RequestMapping("/dataset/field")
public class DataSetTableFieldController {
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

    @Autowired
    private DataSetFieldService dataSetFieldService;

    @PostMapping("list/{tableId}")
    public List<DatasetTableField> list(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        return dataSetTableFieldsService.list(datasetTableField);
    }

    @PostMapping("listByDQ/{tableId}")
    public Map<String, List<DatasetTableField>> listByDQ(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        datasetTableField.setGroupType("d");
        List<DatasetTableField> dimensionList = dataSetTableFieldsService.list(datasetTableField);
        datasetTableField.setGroupType("q");
        List<DatasetTableField> quotaList = dataSetTableFieldsService.list(datasetTableField);

        Map<String, List<DatasetTableField>> map = new HashMap<>();
        map.put("dimensionList", dimensionList);
        map.put("quotaList", quotaList);
        return map;
    }

    @PostMapping("batchEdit")
    public void batchEdit(@RequestBody List<DatasetTableField> list) {
        dataSetTableFieldsService.batchEdit(list);
    }

    @PostMapping("save")
    public DatasetTableField save(@RequestBody DatasetTableField datasetTableField) {
        return dataSetTableFieldsService.save(datasetTableField);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableFieldsService.delete(id);
    }

    @PostMapping("fieldValues/{fieldId}")
    public List<Object> fieldValues(@PathVariable String fieldId) {
        return dataSetFieldService.fieldValues(fieldId);
    }
}
