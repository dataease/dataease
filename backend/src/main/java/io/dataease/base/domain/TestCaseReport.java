package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReport implements Serializable {
    private String id;

    private String name;

    private Long startTime;

    private Long endTime;

    private String content;

    private static final long serialVersionUID = 1L;
}
