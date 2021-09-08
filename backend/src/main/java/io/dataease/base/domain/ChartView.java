package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ChartView implements Serializable {
    private String id;

    private String name;

    private String sceneId;

    private String tableId;

    private String type;

    private String title;

    private String createBy;

    private Long createTime;

    private Long updateTime;

    private String stylePriority;

    private static final long serialVersionUID = 1L;
}