package io.dataease.plugins.xpack.email.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class XpackReportExportRequest implements Serializable {

    private String panelId;

    private boolean showPageNo;

    private String pixel;
}
