package io.dataease.dto.panel;

import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.plugins.common.base.domain.PanelGroupWithBLOBs;
import io.dataease.plugins.common.model.ITreeBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelGroupDTO extends PanelGroupWithBLOBs implements ITreeBase<PanelGroupDTO> {
    @ApiModelProperty("创建用户名")
    private String creatorName;
    @ApiModelProperty("更新用户名")
    private String updateName;
    @ApiModelProperty("标签")
    private String label;
    @ApiModelProperty("是否叶子节点")
    private Boolean leaf;
    @ApiModelProperty("权限")
    private String privileges;
    @ApiModelProperty("默认仪表板ID")
    private String defaultPanelId;
    @ApiModelProperty("默认仪表板名称")
    private String defaultPanelName;
    @ApiModelProperty("是否默认")
    private Boolean isDefault;
    @ApiModelProperty("源仪表板名称")
    private String sourcePanelName;
    @ApiModelProperty("子节点")
    private List<PanelGroupDTO> children;
    @ApiModelProperty("视图信息")
    private List<Map<String, ChartViewDTO>> viewsInfo;
    @ApiModelProperty("父级ID")
    private List<String> parents;
    @ApiModelProperty("请求ID")
    private String requestId;
    @ApiModelProperty("数据返回来源")
    private String responseSource;
}
