package io.dataease.datasource.provider;

import io.dataease.utils.CommonBeanFactory;
import io.micrometer.common.util.StringUtils;

public class ProviderUtil {


    public static EngineProvider getEngineProvider(String datasourceType) {
        if (StringUtils.isNotEmpty(datasourceType)) {
            return (EngineProvider) CommonBeanFactory.getBean(datasourceType + "Engine");
        } else {
            return CommonBeanFactory.getBean(MysqlEngineProvider.class);
        }
    }

}
