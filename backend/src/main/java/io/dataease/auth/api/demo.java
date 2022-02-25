package io.dataease.auth.api;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("api/data")
public class demo {
    @ApiOperation("查询")
    @GetMapping("demo")
    public Object listByTableId() {
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<10;i++){
            JSONObject jsonObject = new JSONObject();
            for(int j=0;j<1;j++){
                jsonObject.set("column" +i + j, "value"+i+j);
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
