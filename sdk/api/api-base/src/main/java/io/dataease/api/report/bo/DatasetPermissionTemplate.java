package io.dataease.api.report.bo;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

@Data
public class DatasetPermissionTemplate {

    private Long datasetId;

    private Set<Long> dsIdSet;

    private List<TableSysVariable> tableSysVariables;

    public boolean hasSysVariable() {
        return CollectionUtils.isNotEmpty(tableSysVariables);
    }
}
