package io.dataease.api.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Schema(description = "导出请求")
@Data
public class ReportExportRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5372551595882128201L;
    @Schema(description = "资源ID")
    private Long resourceId;
    @Schema(description = "资源类型")
    private String busiType;
    @Schema(description = "分辨率")
    private String pixel;
    @Schema(description = "超时时间")
    private Integer extWaitTime = 0;
    @Schema(description = "渲染时间")
    private Integer renderTime = 2;
    @Schema(description = "导出格式")
    private Integer resultFormat = 0;
}
