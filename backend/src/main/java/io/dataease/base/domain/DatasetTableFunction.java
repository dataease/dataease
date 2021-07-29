package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTableFunction implements Serializable {
    private Long id;

    private String name;

    private String func;

    private String dbType;

    private Integer funcType;

    private String desc;

    private static final long serialVersionUID = 1L;
}