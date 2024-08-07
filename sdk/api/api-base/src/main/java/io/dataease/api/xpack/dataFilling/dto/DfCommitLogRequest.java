package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DfCommitLogRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8186544152722934919L;

    private Long formId;

    private Integer operate;
}
