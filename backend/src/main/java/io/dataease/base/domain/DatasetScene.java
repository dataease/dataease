package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetScene implements Serializable {
    private String id;

    private String name;

    private String groupId;

    private String createBy;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}