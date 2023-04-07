package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class OrgPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7788232223396601785L;

    @JsonSerialize(using= ToStringSerializer.class)
    @ApiModelProperty("组织ID")
    private Long id;

    @ApiModelProperty("组织名称")
    private String name;

    @ApiModelProperty("创建时间")
    private Long createTime;

    @ApiModelProperty("只读")
    private boolean readOnly = true;

    @ApiModelProperty("子组织")
    private List<OrgPageVO> children;
}
