package io.dataease.exportCenter.manage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("exportCenterLimitManage")
public class ExportCenterLimitManage {

    @Value("${dataease.export.dataset.limit:100000}")
    private Long datasetLimit;

    @Value("${dataease.export.views.limit:100000}")
    private Long viewLimit;

    public Long getExportLimit(String type) {
        if (StringUtils.isBlank(type) || StringUtils.equals("view", type)) {
            return viewLimit;
        }
        return datasetLimit;
    }
}
