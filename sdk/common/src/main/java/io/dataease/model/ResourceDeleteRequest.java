package io.dataease.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "资源删除请求")
@Data
public class ResourceDeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -5488509404104405459L;

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "是否组织同名根目录(虚拟目录，业务表无对应记录)")
    private Boolean rootOrgNode;

    @Schema(description = "业务标识(dashboard/dataV)，仅可视化资源使用")
    private String busiFlag;
}
