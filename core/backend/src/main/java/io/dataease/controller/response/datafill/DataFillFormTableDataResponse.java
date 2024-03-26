package io.dataease.controller.response.datafill;

import io.dataease.plugins.common.dto.datafill.ExtTableField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class DataFillFormTableDataResponse implements Serializable {

    private static final long serialVersionUID = -6463885075511811532L;

    private Object data;

    private List<ExtTableField> fields;

    private long total;

    private long currentPage;

    private long pageSize;

    private String key;

}
