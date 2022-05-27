package io.dataease.controller.request.datasource;

import com.google.gson.JsonObject;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApiDefinitionRequest {
    private List<JsonObject> headers = new ArrayList<>();
    private JsonObject body = new JsonObject();
    private AuthManager authManager = new AuthManager();


    @Data
    public static class AuthManager{
        private String password;
        private String username;
        private String verification = "";
    }
}
