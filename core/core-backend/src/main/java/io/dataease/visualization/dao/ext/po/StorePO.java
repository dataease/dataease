package io.dataease.visualization.dao.ext.po;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class StorePO implements Serializable {
    @Serial
    private static final long serialVersionUID = 9130790627765997999L;

    private Long resourceId;

    private String type;

    private String creator;

    private String editor;

    private Long editTime;

    private Long storeId;

    private String name;

    private Integer extFlag;

    private Integer extFlag1;

}
