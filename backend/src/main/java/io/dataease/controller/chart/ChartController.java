package io.dataease.controller.chart;

import com.alibaba.fastjson.JSON;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("chart/table")
public class ChartController {



    @PostMapping("list")
    public List<JSON> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return new ArrayList<>();
    }

}
