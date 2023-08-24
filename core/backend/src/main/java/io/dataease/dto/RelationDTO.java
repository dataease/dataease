package io.dataease.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WiSoniC
 * @date 2022年12月8日18:53:10
 */
@Data
public class RelationDTO {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("权限信息")
    private String auths;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("被引用信息")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<RelationDTO> subRelation;
}
