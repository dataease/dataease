package io.dataease.dto.datasource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DorisConfiguration extends MysqlConfiguration {

    private Integer httpPort = 8030;

    private Integer replicationNum = 1;
    private Integer bucketNum = 10;
}
