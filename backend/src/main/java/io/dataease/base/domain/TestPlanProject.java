package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanProject implements Serializable {
    private String testPlanId;

    private String projectId;

    private static final long serialVersionUID = 1L;
}
