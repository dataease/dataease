package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExtraColumnItem {
    private String fieldName;
    private String displayName;
}
