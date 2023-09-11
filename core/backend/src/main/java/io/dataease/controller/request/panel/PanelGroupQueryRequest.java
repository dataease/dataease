package io.dataease.controller.request.panel;

import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/4/20
 * Description:
 */
@Data
public class PanelGroupQueryRequest {

    private String panelId;

    private String panelType; //仪表板类型 self 普通仪表板 or system默认仪表板

    private String queryFrom; //查询来源 edit 编辑初始化查询

}
