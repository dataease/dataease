package io.dataease.visualization.manage;

import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("coreBusiManage")
public class CoreBusiManage {

    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    @Resource
    private DataSourceManage dataSourceManage;

    @Resource
    private DatasetGroupManage datasetGroupManage;

    public Map<String, List<BusiNodeVO>> interactiveTree(Map<String, BusiNodeRequest> requestMap) {
        Map<String, List<BusiNodeVO>> result = new HashMap<>();
        for (Map.Entry<String, BusiNodeRequest> entry : requestMap.entrySet()) {
            BusiNodeRequest busiNodeRequest = entry.getValue();
            String key = entry.getKey();
            if (StringUtils.equalsIgnoreCase(key, "datasource")) {
                result.put(key, dataSourceManage.tree(busiNodeRequest));
            } else if (StringUtils.equalsIgnoreCase(key, "dataset")) {
                result.put(key, datasetGroupManage.tree(busiNodeRequest));
            } else if (StringUtils.equalsAnyIgnoreCase(key, "dashboard", "dataV")) {
                result.put(key, coreVisualizationManage.tree(busiNodeRequest));
            }
        }
        return result;
    }
}
