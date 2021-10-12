package io.dataease.datasource.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DorisConfiguration extends MysqlConfiguration {

    private Integer httpPort;
}
