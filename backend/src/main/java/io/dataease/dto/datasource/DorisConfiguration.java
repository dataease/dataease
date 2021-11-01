package io.dataease.dto.datasource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DorisConfiguration extends MysqlConfiguration {

    private Integer httpPort;
}
