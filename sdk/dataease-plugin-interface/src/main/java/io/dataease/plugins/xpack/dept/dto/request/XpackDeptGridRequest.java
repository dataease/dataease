package io.dataease.plugins.xpack.dept.dto.request;

import io.dataease.plugins.common.request.KeywordRequest;
import lombok.Data;

@Data
public class XpackDeptGridRequest extends KeywordRequest {

    private Long pid = 0L;
}
