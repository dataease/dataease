package io.dataease.extensions.datasource.factory;

import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * @Author Junjun
 */
public class ProviderFactory {

    public static Provider getProvider(String type) {
        List<String> list = Arrays.stream(DatasourceConfiguration.DatasourceType.values()).map(DatasourceConfiguration.DatasourceType::getType).toList();
        if (list.contains(type)) {
            return SpringContextUtil.getApplicationContext().getBean("calciteProvider", Provider.class);
        }
        return SpringContextUtil.getApplicationContext().getBean(type + "DsProvider", Provider.class);
    }
}
