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
public class TaskInfoGridVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5178055146669970633L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private Long lastExecTime;

    private Integer lastExecStatus;

    private Integer status;

    private Long nextExecTime;

    private String creatBy;

    private String creator;

    private Long createTime;

}
