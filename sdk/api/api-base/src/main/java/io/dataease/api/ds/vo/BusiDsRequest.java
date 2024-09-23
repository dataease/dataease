package io.dataease.api.ds.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.datasource.dto.TaskDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiDsRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1175287571828910222L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "父ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pid;
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String  nodeType;
    @Schema(description = "操作类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String  action;
    private String description;
    @Schema(description = "数据源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
    private String typeAlias;
    private String catalog;
    private String catalogDesc;
    @Schema(description = "详细信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configuration;
    private String apiConfigurationStr;
    private String paramsStr;
    private Long createTime;
    private Long updateTime;
    private Long updateBy;
    private String createBy;
    private String creator;
    private String status;
    private TaskDTO syncSetting;
    private Integer editType;
    private String  fileName;
    private String  size;
    private Long lastSyncTime;
    private String qrtzInstance;
    private String taskStatus;
    private Boolean enableDataFill;
}
