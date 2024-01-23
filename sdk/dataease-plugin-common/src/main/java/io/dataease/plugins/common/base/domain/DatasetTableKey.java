package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTableKey implements Serializable {
    private String id;

    private String tableId;

    private String tableFieldId;

    private static final long serialVersionUID = 1L;
}