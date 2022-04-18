package io.datains.dto.panel.linkJump;

import io.datains.base.domain.PanelLinkJumpInfo;
import io.datains.base.domain.PanelLinkJumpTargetViewInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021/10/25
 * Description:
 */
@Data
public class PanelLinkJumpInfoDTO extends PanelLinkJumpInfo {

    private String sourceFieldName;

    private String sourceJumpInfo;

    //存在公共链接的目标仪表板
    private String publicJumpId;

    private List<PanelLinkJumpTargetViewInfo> targetViewInfoList=new ArrayList<>();// linkType = inner 时使用


}
