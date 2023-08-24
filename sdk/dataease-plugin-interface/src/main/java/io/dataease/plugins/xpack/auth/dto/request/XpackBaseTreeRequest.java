package io.dataease.plugins.xpack.auth.dto.request;

import io.dataease.plugins.common.constants.PluginSystemConstants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XpackBaseTreeRequest {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;

    //授权资源类型 eg：chart role
    @ApiModelProperty("授权类型")
    private String modelType;

    //pid 为0 时 查询的是顶级节点 SQL需要相应的处理
    @ApiModelProperty("父节点")
    private String pid;

    //now 返回当前条件查询的数据 parent 返回当前数据查询的数据同时递归父节点数据; children 返回当前数据查询的数据同时递归子节点数据
    @ApiModelProperty("扩展类型")
    private String withExtend= PluginSystemConstants.WITH_EXTEND.NOW;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("需要根据权限返回")
    private String withAuth;//需要根据权限返回
    @ApiModelProperty("资源内部类型")
    private String modelInnerType;


    public XpackBaseTreeRequest() {
    }

    public XpackBaseTreeRequest(String id,String modelType, String withExtend) {
        this.id = id;
        this.modelType = modelType;
        this.withExtend = withExtend;
    }
}
