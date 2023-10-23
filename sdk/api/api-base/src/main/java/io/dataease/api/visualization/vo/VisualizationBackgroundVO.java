package io.dataease.api.visualization.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : WangJiaHao
 * @date : 2023/6/12 19:27
 */
@Data
public class VisualizationBackgroundVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String classification;

    private String content;

    private String remark;

    private Integer sort;

    private Long uploadTime;

    private String baseUrl;

    private String url;

}
