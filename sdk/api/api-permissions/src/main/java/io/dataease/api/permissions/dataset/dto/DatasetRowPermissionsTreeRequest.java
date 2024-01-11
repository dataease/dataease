package io.dataease.api.permissions.dataset.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author gin
 * @Date 2022/7/19 20:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DatasetRowPermissionsTreeRequest extends DataSetRowPermissionsTreeDTO {
    public String orderBy;
}
