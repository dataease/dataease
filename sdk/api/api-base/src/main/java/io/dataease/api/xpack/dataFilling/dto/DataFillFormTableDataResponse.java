package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class DataFillFormTableDataResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -6463885075511811532L;

    private Object data;

    private String fields;

    private long total;

    private long currentPage;

    private long pageSize;

    private String key;

}
