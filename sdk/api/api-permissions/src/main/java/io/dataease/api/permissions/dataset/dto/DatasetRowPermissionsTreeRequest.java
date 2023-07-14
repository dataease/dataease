package io.dataease.api.permissions.dataset.dto;

import lombok.Data;

/**
 * @Author gin
 * @Date 2022/7/19 20:23
 */
@Data
public class DatasetRowPermissionsTreeRequest extends DataSetRowPermissionsTreeDTO {
    public String orderBy;
}
