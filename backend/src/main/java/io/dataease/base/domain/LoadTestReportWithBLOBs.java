package io.dataease.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoadTestReportWithBLOBs extends LoadTestReport implements Serializable {
    private String description;

    private String loadConfiguration;

    private static final long serialVersionUID = 1L;
}
