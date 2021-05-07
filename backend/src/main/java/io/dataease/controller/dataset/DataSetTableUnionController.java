package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableUnion;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.service.dataset.DataSetTableUnionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/7 10:30 上午
 */
@RestController
@RequestMapping("dataset/union")
public class DataSetTableUnionController {
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;

    @PostMapping("save")
    public DatasetTableUnion save(@RequestBody DatasetTableUnion datasetTableUnion) {
        return dataSetTableUnionService.save(datasetTableUnion);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableUnionService.delete(id);
    }

    @PostMapping("listByTableId/{tableId}")
    public List<DataSetTableUnionDTO> listByTableId(@PathVariable String tableId) {
        return dataSetTableUnionService.listByTableId(tableId);
    }
}
