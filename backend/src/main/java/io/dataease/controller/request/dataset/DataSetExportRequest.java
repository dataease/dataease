package io.dataease.controller.request.dataset;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class DataSetExportRequest extends DataSetTableRequest {
    private String filename;
    private String expressionTree;
}
