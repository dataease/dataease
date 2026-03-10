package io.dataease.api.permissions.dataset.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RowColPermissionItem implements Serializable {

    private Long id;

    private boolean enable;

    private String authTargetType;

    private String authTargetId;

    private Long datasetId;

    private String permissionText;

    private List<Long> whiteListUserIds;

    private String type;

    private List<ColPermissionInfo> colPermissionInfos;

}
