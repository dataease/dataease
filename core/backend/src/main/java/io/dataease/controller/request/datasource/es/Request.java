package io.dataease.controller.request.datasource.es;

import lombok.Data;

@Data
public class Request {
    private String query;
    private Integer fetch_size = 10000;
    private boolean field_multi_value_leniency = true;
}
