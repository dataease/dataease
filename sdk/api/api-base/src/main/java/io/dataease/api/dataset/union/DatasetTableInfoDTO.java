package io.dataease.api.dataset.union;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author gin
 */
@Data
public class DatasetTableInfoDTO implements Serializable {
    private String table;
    private String sql;
}
