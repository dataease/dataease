package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DfUserTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 2573431692696582629L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;

    private String taskName;

    private Long startTime;

    private Long endTime;

    private Long finishTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignBy;

    private String assigner;

    private long totalCount;

    private long finishCount;

    private boolean expired;

    private Integer fillType;

}
