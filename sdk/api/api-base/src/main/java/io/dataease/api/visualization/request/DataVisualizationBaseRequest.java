package io.dataease.api.visualization.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationExport2AppVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private List<Long> activeViewIds;

    // 查询来源 main-edit= 主工程编辑区 main=主工程 report=定时报告
    private String source;

    // 定时报告id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    // 定时报告任务id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private Boolean showWatermark;

    @JsonSerialize(using = ToStringSerializer.class)
    // 数据集分组PID
    private Long datasetFolderPid;

    // 数据集分组名称
    private String datasetFolderName;

    //新赋值的content_id
    private String newContentId;

    // 是否强制校验新旧contentId
    private Boolean checkHistory = false;

    //数据来源 core 主表 snapshot 镜像表
    private String resourceTable = "core";

    public DataVisualizationBaseRequest(Long id,String busiFlag,String resource,String source) {
        this.busiFlag = busiFlag;
        this.resourceTable = resource;
        super.setId(id);
        this.setSource(source);
    }

    public DataVisualizationBaseRequest(Long id,String busiFlag) {
        this.busiFlag = busiFlag;
        super.setId(id);
    }
}
