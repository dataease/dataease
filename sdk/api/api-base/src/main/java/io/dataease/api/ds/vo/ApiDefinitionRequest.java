package io.dataease.api.ds.vo;


import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ApiDefinitionRequest {
    private List<Map<String, String>> headers = new ArrayList<>();
    private Map<String, Object> body = new HashMap<>();
    private List<Map<String, String>> arguments = new ArrayList<>();
    private List<Map<String, String>> rest = new ArrayList<>();
    private AuthManager authManager = new AuthManager();


    @Data
    public static class AuthManager{
        private String password;
        private String username;
        private String verification = "";
    }
}
