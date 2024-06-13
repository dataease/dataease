package io.dataease.api.exportCenter.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ExportTaskDTO  {
    @JsonSerialize(using= ToStringSerializer.class)
    private String id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long userId;

    private String fileName;

    private Double fileSize;

    private String fileSizeUnit;

    private String exportFrom;

    private String exportStatus;

    private String msg;

    private String exportFromType;

    private Long exportTime;

    private String exportProgress;

    private String exportMachineName;

    private String exportFromName;

    private String orgName;
}
