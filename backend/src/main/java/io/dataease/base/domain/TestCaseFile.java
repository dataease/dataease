package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseFile implements Serializable {
    private String caseId;

    private String fileId;

    private static final long serialVersionUID = 1L;
}
