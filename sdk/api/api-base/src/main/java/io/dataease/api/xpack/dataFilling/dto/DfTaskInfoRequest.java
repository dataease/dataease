package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DfTaskInfoRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3478936079850972546L;

    private List<Long> uidList;

    private List<Integer> lastStatusList;

    private List<Integer> statusList;

    private List<Long> timeList;

    private String keyword;

    private Boolean timeDesc;
}
