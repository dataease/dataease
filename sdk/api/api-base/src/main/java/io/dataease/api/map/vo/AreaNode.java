package io.dataease.api.map.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "区域节点")
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -2285934203102231711L;
    @Schema(description = "ID")
    private String id;
    @Schema(description = "级别")
    private String level;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "上级ID")
    private String pid;
    @Schema(description = "是否自定义节点")
    private boolean custom = false;
    /**
     * 国家代码
     */
    @Schema(description = "国家代码")
    private String country;
    /**
     * 下属区域
     */
    @Schema(description = "子集")
    private List<AreaNode> children;
}
