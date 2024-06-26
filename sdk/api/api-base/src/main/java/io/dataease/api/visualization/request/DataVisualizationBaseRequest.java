package io.dataease.api.visualization.request;

import io.dataease.api.visualization.vo.DataVisualizationVO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

    private String busiFlag;

    // 查询来源 main=主工程 report=定时报告
    private String source;

    // 定时报告id
    private Long reportId;

    // 定时报告任务id
    private Long taskId;


    public DataVisualizationBaseRequest(Long id,String busiFlag) {
        this.busiFlag = busiFlag;
        super.setId(id);
    }
}
