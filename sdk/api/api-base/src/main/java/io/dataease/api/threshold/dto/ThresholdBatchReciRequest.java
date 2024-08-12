package io.dataease.api.threshold.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThresholdBatchReciRequest extends BaseReciDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2831988863396898760L;

    private List<Long> idList;

}
