package io.dataease.controller.request.datasource;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ApiDefinitionRequest {
    private List<Map<String, String>> headers = new ArrayList<>();
    private List<Map<String, String>> arguments = new ArrayList<>();
    private JSONObject body = new JSONObject();
    private AuthManager authManager = new AuthManager();


    @Data
    public static class AuthManager{
        private String password;
        private String username;
        private String verification = "";
    }
}
