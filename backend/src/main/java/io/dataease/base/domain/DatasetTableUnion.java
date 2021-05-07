package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTableUnion implements Serializable {
    private String id;

    private String sourceTableId;

    private String sourceTableFieldId;

    private String sourceUnionRelation;

    private String targetTableId;

    private String targetTableFieldId;

    private String targetUnionRelation;

    private String createBy;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}