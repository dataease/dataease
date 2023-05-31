package io.dataease.api.dataset.dto;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class PreviewSqlDTO {
    private String sql;
    private Long datasourceId;
}
