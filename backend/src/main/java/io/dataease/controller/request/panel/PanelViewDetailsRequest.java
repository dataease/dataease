package io.dataease.controller.request.panel;

import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/4/8
 * Description:
 */
@Data
public class PanelViewDetailsRequest {

    private String viewId;

    private String viewName;

    private String[] header;

    private Integer[] excelTypes;

    private List<String[]> details;

    private String snapshot;

    private int snapshotWidth;

    private int snapshotHeight;



}
