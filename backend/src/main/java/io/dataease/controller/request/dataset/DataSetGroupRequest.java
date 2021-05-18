package io.dataease.controller.request.dataset;

import io.dataease.base.domain.DatasetGroup;
import lombok.Data;

/**
 * @Author gin
 * @Date 2021/2/22 1:30 下午
 */
@Data
public class DataSetGroupRequest extends DatasetGroup {
    private String sort;

    private String userId;
}
