package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DfCommitLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 3175509273615697110L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;

    private String dataId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 操作 0删除 1插入 2更新
     */
    private Integer operate;

    private Long commitBy;

    private String committer;

    private Long commitTime;

    private Integer count;

    public DfCommitLog(Long id, Long formId, String dataId, Integer operate, Long commitBy, Long commitTime, Integer count, String committer) {
        this.id = id;
        this.formId = formId;
        this.dataId = dataId;
        this.operate = operate;
        this.commitBy = commitBy;
        this.commitTime = commitTime;
        this.count = count;
        this.committer = committer;
    }

    public DfCommitLog(Long id, Long formId, String dataId, Long pid, Integer operate, Long commitBy, String committer, Long commitTime, Integer count) {
        this.id = id;
        this.formId = formId;
        this.dataId = dataId;
        this.pid = pid;
        this.operate = operate;
        this.commitBy = commitBy;
        this.committer = committer;
        this.commitTime = commitTime;
        this.count = count;
    }
}
