package io.dataease.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiNodeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3859379188035689316L;

    private String busyFlag;

    private String id;

    private Boolean leaf;

    private Integer weight;
}
