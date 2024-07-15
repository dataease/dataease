package io.dataease.api.visualization.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class VisualizationAppExportRequest {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dvId;

    @JsonSerialize(using = ToStringSerializer.class)
    private List<Long> viewIds;

    @JsonSerialize(using = ToStringSerializer.class)
    private List<Long> dsIds;
}
