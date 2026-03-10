package io.dataease.api.permissions.dataset.bo;

import io.dataease.api.permissions.dataset.vo.ColPermissionInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ColPermissionBo implements Serializable {

    private boolean enable;

    private List<ColPermissionInfo> columns;
}
