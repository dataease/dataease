package io.dataease.api.spreadsheet;

import io.dataease.api.visualization.vo.VisualizationLinkJumpTargetViewInfoVO;

import java.util.List;

/** 电子表格跳转的内部校验能力，正式稿与权限实现由扩展提供。 */
public interface SpreadsheetLinkJumpService {

    void validateTargets(Long spreadsheetId, List<VisualizationLinkJumpTargetViewInfoVO> targets);
}
