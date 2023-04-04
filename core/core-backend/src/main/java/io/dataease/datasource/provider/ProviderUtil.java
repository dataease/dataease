package io.dataease.datasource.provider;

import io.dataease.utils.CommonBeanFactory;
import io.micrometer.common.util.StringUtils;

public class ProviderUtil {

    public static Provider getProvider(String datasourceType) {
        if(StringUtils.isNotEmpty(datasourceType) && datasourceType.equals("API")){
            return CommonBeanFactory.getBean(ApiProvider.class);
        }else {
            return CommonBeanFactory.getBean(CalciteProvider.class);
        }
    }
}
