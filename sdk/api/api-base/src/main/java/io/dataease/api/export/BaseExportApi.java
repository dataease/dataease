package io.dataease.api.export;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.HashMap;

@Hidden
public interface BaseExportApi {

    void addTask(String exportFromId, String exportFromType, HashMap<String, Object> request, Long userId, Long org);


}
