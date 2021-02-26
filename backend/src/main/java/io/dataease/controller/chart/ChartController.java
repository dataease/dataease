package io.dataease.controller.chart;

import com.alibaba.fastjson.JSON;
import io.dataease.base.domain.DatasetTable;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.service.dataset.DataSetTableService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chart/table")
public class ChartController {



    @PostMapping("list")
    public List<JSON> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return new ArrayList<>();
    }

}
