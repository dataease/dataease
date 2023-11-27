package io.dataease.api.visualization.request;

import io.dataease.api.visualization.vo.DataVisualizationVO;
import lombok.Data;

@Data
public class DataVisualizationBaseRequest extends DataVisualizationVO {

    private String opt;

    private String resourceName;

    private Boolean moveFromUpdate = false;

    private String optType;

    private String newFrom;

    private String dynamicData;

    private String templateId;

    private String staticResource;

    private String templateUrl;

}
