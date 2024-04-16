package io.dataease.api.dataset.dto;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class EnumValueRequest {
    private Long queryId;
    private Long displayId;
    private Long sortId;
    private String sort;
}
