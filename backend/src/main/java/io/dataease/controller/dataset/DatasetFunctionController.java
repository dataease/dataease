package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetTableFunction;
import io.dataease.service.dataset.DatasetFunctionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/7/29 11:58 上午
 */
@RestController
@RequestMapping("dataset/function")
public class DatasetFunctionController {
    @Resource
    private DatasetFunctionService datasetFunctionService;

    @PostMapping("listByTableId/{tableId}")
    public List<DatasetTableFunction> listByTableId(@PathVariable String tableId) {
        return datasetFunctionService.listByTableId(tableId);
    }
}
