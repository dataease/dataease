package io.dataease.controller.dataset.request;

import io.dataease.plugins.common.request.KeywordRequest;
import lombok.Data;

import java.util.List;

@Data
public class DataSetTaskInstanceGridRequest extends KeywordRequest {

    private String id;

    private List<String> lastExecStatus;

    private Long[] lastExecTime;

    private List<String> tableId;

    private List<String> excludedIdList;
}
