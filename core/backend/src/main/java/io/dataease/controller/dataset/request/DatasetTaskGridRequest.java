package io.dataease.controller.dataset.request;

import io.dataease.plugins.common.request.KeywordRequest;
import lombok.Data;

import java.util.List;

@Data
public class DatasetTaskGridRequest extends KeywordRequest {

    private List<String> rate;

    private List<String> status;

    private List<String> lastExecStatus;

    private List<String> tableId;

    private Long[] lastExecTime;

    private String id;

    private Long userId;
}
