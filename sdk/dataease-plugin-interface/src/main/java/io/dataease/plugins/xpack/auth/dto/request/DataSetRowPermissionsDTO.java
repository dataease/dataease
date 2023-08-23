package io.dataease.plugins.xpack.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataSetRowPermissionsDTO extends DatasetRowPermissions {
    private String datasetName;
    private String fieldName;
    private String authTargetName;
    private List<Long> authTargetIds;
}
