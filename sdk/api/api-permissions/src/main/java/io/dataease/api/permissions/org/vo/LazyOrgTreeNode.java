package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "组织列表VO")
@Data
public class LazyOrgTreeNode implements Serializable {
    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @Schema(description = "PID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long pid;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "只读")
    private boolean readOnly = true;
    @Schema(description = "有子集")
    private boolean hasChildren;
    @Schema(description = "子集")
    private List<LazyOrgTreeNode> children;
}
