package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTableIncrementalConfig implements Serializable {
    private String id;

    private String tableId;

    private String incrementalDelete;

    private String incrementalAdd;

    private static final long serialVersionUID = 1L;
}