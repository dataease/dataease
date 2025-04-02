package io.dataease.extensions.datasource.dto;


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
    private Page page = new Page();


    @Data
    public static class AuthManager {
        private String password;
        private String username;
        private String verification = "";
    }

    @Data
    public static class Page {
        private String pageType = "empty";
        private List<RequestItem> requestData;
        private List<ResponseItem> responseData;
    }

    @Data
    public static class RequestItem {
        private String parameterName;
        private String builtInParameterName;
        private String requestParameterName;
        private String parameterDefaultValue;
    }

    @Data
    public static class ResponseItem {
        private String parameterName;
        private String resolutionPath;
        private String resolutionPathType;
    }

}
