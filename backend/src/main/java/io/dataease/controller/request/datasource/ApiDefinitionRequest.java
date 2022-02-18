package io.dataease.controller.request.datasource;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApiDefinitionRequest {
    private List<JSONObject> headers = new ArrayList<>();
    private JSONObject body = new JSONObject();
    private AuthManager authManager = new AuthManager();


    @Data
    public class AuthManager{
        private String password;
        private String username;
        private String verification = "";
    }
}
