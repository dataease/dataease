package io.dataease.datasource.dto.es;

import lombok.Data;

@Data
public class Requst {
    private String query;
    private Integer fetch_size = 10000;
}
