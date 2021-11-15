package io.dataease.dto.panel.linkJump;

import io.dataease.base.domain.PanelLinkJumpInfo;
import io.dataease.base.domain.PanelLinkJumpTargetViewInfo;
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

    private List<PanelLinkJumpTargetViewInfo> targetViewInfoList=new ArrayList<>();// linkType = inner 时使用


}
