package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
@Accessors(chain = true)
public class DataFillFormTableDataSearchParam implements Serializable {

    @Serial
    private static final long serialVersionUID = -9094306073413857266L;

    private String term;

    private String field;

    private Object value;

    private List<Object> values;

    private boolean multiple;

}
