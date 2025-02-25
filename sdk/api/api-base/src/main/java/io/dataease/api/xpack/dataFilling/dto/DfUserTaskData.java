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
public class DfUserTaskData implements Serializable {
    @Serial
    private static final long serialVersionUID = -3635512734559897349L;

    @Data
    @Accessors(chain = true)
    public static class DfSubInstance implements Serializable {
        @Serial
        private static final long serialVersionUID = 4847820854888692033L;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long taskId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long pid;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long uid;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long formId;
        @JsonSerialize(using = ToStringSerializer.class)
        private String dataId;

        private Long finishTime;
        private Integer status;

    }

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;
    private String formTitle;
    //@JsonSerialize(using = LongArray2StringSerialize.class)
    private List<String> dataIds;

    private List<DfSubInstance> subInstances;

    private String form;

    private String formExtSetting;


}
