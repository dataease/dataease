package io.dataease.plugins.xpack.email.dto.request;

import io.dataease.plugins.common.request.KeywordRequest;
import lombok.Data;

import java.util.List;

@Data
public class XpackEmailInstanceGridRequest extends KeywordRequest {

    private List<String> statusList;

    private Long[] timeArray;

    private Long taskId;
}
