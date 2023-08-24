package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QrtzSchedulerState extends QrtzSchedulerStateKey implements Serializable {
    private Long lastCheckinTime;

    private Long checkinInterval;

    private static final long serialVersionUID = 1L;
}