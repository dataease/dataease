package io.dataease.plugins.common.request.permission;

import lombok.Data;

/**
 * @Author gin
 * @Date 2022/7/19 20:23
 */
@Data
public class DatasetRowPermissionsTreeRequest extends DataSetRowPermissionsTreeDTO {
    public String orderBy;
}
