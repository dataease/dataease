package io.dataease.controller.sys.request;

import io.dataease.plugins.common.request.KeywordRequest;
import lombok.Data;

import java.util.List;

@Data
public class LogGridRequest extends KeywordRequest {

    private List<String> optypeList;

    private List<Long> userIdList;

    private Long[] timeList;
}
