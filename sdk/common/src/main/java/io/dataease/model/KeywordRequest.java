package io.dataease.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "关键紫过滤器")
@Data
public class KeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3038086304525253475L;
    @Schema(description = "关键字")
    private String keyword;
}
