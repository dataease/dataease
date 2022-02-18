package io.dataease.auth.api;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.DatasetTableFunction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/data")
public class demo {
    @ApiOperation("查询")
    @GetMapping("demo")
    public Object listByTableId() {
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<100;i++){
            JSONObject jsonObject = new JSONObject();
            for(int j=0;j<10;j++){
                jsonObject.set("column" + j, "value"+j);
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
