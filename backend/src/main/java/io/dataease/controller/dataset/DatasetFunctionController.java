package io.dataease.controller.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.DatasetTableFunction;
import io.dataease.service.dataset.DatasetFunctionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "数据集：数据集方法")
@ApiSupport(order = 80)
@RestController
@RequestMapping("dataset/function")
public class DatasetFunctionController {
    @Resource
    private DatasetFunctionService datasetFunctionService;

    @ApiOperation("查询")
    @PostMapping("listByTableId/{tableId}")
    public List<DatasetTableFunction> listByTableId(@PathVariable String tableId) {
        return datasetFunctionService.listByTableId(tableId);
    }
}
