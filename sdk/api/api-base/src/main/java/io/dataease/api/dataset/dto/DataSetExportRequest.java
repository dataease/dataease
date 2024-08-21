package io.dataease.api.dataset.dto;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class DataSetExportRequest extends DatasetNodeDTO {
    private String filename;
    private String expressionTree;
}
