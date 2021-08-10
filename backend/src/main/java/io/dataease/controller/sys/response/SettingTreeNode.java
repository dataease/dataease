package io.dataease.controller.sys.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SettingTreeNode implements Serializable {

    private static final long serialVersionUID = -2416283978185545199L;

    @ApiModelProperty("消息类型ID")
    private Long id;

    @ApiModelProperty("消息类型名称")
    private String name;

    @ApiModelProperty("子节点")
    private List<SettingTreeNode> children;
}
