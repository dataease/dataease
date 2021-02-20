package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestPlanTestCaseWithBLOBs extends TestPlanTestCase implements Serializable {
    private String results;

    private String issues;

    private static final long serialVersionUID = 1L;
}
