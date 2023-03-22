package io.dataease.api.dataset.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetTreeNodeVO implements Serializable {

    private Long id;

    private String name;

    private String type;
}
