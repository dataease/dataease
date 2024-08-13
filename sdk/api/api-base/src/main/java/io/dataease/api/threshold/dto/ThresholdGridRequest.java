package io.dataease.api.threshold.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ThresholdGridRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -2729126999572515149L;

    private String keyword;

    private List<String> resourceTypeList;

    private List<Integer> statusList;

    private List<Integer> enableList;

    private List<Long> timeList;

    private Long chartId;

}
