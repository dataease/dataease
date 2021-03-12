package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableField;
import io.dataease.service.dataset.DataSetTableFieldsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/24 4:28 下午
 */
@RestController
@RequestMapping("/dataset/field")
public class DataSetTableFieldController {
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

    @PostMapping("list/{tableId}")
    public List<DatasetTableField> list(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        return dataSetTableFieldsService.list(datasetTableField);
    }

    @PostMapping("batchEdit")
    public void batchEdit(@RequestBody List<DatasetTableField> list) {
        dataSetTableFieldsService.batchEdit(list);
    }
}
