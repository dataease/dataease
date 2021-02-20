package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseComment implements Serializable {
    private String id;

    private String caseId;

    private String author;

    private Long createTime;

    private Long updateTime;

    private String description;

    private static final long serialVersionUID = 1L;
}
