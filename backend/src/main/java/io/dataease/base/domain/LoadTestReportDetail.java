package io.dataease.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoadTestReportDetail extends LoadTestReportDetailKey implements Serializable {
    private String content;

    private static final long serialVersionUID = 1L;
}
