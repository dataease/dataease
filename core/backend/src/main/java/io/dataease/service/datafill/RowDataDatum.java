package io.dataease.service.datafill;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class RowDataDatum implements Serializable {

    private static final long serialVersionUID = -2838784450721979776L;

    private String id;

    private Map<String, Object> data;

    private boolean insert;
}
