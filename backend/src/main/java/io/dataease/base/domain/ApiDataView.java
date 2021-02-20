package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDataView implements Serializable {
    private String id;

    private String reportId;

    private String url;

    private String apiName;

    private String startTime;

    private String responseCode;

    private String responseTime;

    private static final long serialVersionUID = 1L;
}
