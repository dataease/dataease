package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.utils.LongArray2StringSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class TaskInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7074759598819246816L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;

    private String name;

    private List<Integer> reciFlagList;

    @JsonSerialize(using = LongArray2StringSerialize.class)
    private List<Long> uidList;

    @JsonSerialize(using = LongArray2StringSerialize.class)
    private List<Long> ridList;

    private Integer fillType;

    private Integer fitType;

    private String fitColumn;

    private Integer rateType;
    private Integer oneTimeType;

    private String rateVal;

    private Long startTime;

    private Long endTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;
    private String creator;
    private Long createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateBy;
    private String updater;
    private Long updateTime;

    private Integer publishRangeTime;

    private Integer publishRangeTimeType;

    private Integer status;

    private Integer lastExecStatus;
    private Long lastExecTime;
    private Long nextExecTime;


    private String formExtSetting;
    private String formFilterSetting;

}
