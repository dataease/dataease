package io.dataease.controller.request.panel;

import io.dataease.plugins.common.request.KeywordRequest;
import lombok.Data;

@Data
public class AppLogGridRequest extends KeywordRequest {
    private Long[] applyTime;

    private Long userId;
}
