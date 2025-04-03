package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class DfCommitLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 3175509273615697110L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataId;

    /**
     * 操作 0删除 1插入 2更新
     */
    private Integer operate;

    private Long commitBy;

    private String committer;

    private Long commitTime;

    private Integer count;
}
