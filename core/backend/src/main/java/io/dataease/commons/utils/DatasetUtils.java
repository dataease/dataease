package io.dataease.commons.utils;

import io.dataease.plugins.common.dto.dataset.union.UnionDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/11/28
 * Description:
 */
public class DatasetUtils {

    public static void getUnionTable(List<String> tableIdList, List<UnionDTO> childrenDs) {
        if (CollectionUtils.isNotEmpty(childrenDs)) {
            for (UnionDTO unionDTO : childrenDs) {
                String tableId = unionDTO.getCurrentDs().getId();
                tableIdList.add(tableId);
                if (CollectionUtils.isNotEmpty(unionDTO.getChildrenDs())) {
                    getUnionTable(tableIdList, unionDTO.getChildrenDs());
                }
            }
        }
    }
}
