package io.dataease.api.xpack.dataFilling.dto;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class RowDataDatum implements Serializable {

    @Serial
    private static final long serialVersionUID = -2838784450721979776L;

    private Long id;

    private Map<String, Object> data;

    private boolean insert;
}
