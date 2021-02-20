package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTestFile implements Serializable {
    private String testId;

    private String fileId;

    private static final long serialVersionUID = 1L;
}
