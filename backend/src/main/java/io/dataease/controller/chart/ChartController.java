package io.dataease.controller.chart;

import com.alibaba.fastjson.JSON;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "视图：视图管理")
@ApiSupport(order = 110)
@RestController
@RequestMapping("chart/table")
public class ChartController {



    @ApiOperation("查询")
    @PostMapping("list")
    public List<JSON> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return new ArrayList<>();
    }

}
