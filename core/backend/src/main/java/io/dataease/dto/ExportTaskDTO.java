package io.dataease.dto;

import io.dataease.plugins.common.base.domain.ExportTask;
import lombok.Data;

@Data
public class ExportTaskDTO extends ExportTask {
    private String exportFromName;
}
