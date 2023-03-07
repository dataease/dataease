package io.dataease.api.dataset.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataSetTreeNodeVO implements Serializable {

    private Long id;

    private String name;

    private String type;
}
