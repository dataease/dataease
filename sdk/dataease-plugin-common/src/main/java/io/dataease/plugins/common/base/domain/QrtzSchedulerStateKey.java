package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class QrtzSchedulerStateKey implements Serializable {
    private String schedName;

    private String instanceName;

    private static final long serialVersionUID = 1L;
}