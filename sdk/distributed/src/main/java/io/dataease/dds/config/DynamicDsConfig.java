package io.dataease.dds.config;

import com.zaxxer.hikari.HikariDataSource;
import io.dataease.dds.DynamicDataSource;
import io.dataease.dds.constant.DataSourceConstant;
import io.dataease.dds.interceptor.DSInterceptor;
import io.dataease.dds.provider.TenantDatasourceProvider;
import io.dataease.flyway.TenantFlywayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDsConfig implements WebMvcConfigurer {

    @Value("${de.tenant.ds.lazy:false}")
    private Boolean tenantDsLazyLoad;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private DynamicDataSourceProperties properties;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean
    public DynamicDataSource dynamicDataSource(DataSourceProperties dataSourceProperties) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        //设置多个数据源的map
        Map<Object, Object> dynamicDataSourceMap = getDynamicDataSource();
        HikariDataSource defaultDataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        dynamicDataSourceMap.put(DataSourceConstant.DATA_SOURCE_MANAGE, defaultDataSource);
        if (!tenantDsLazyLoad) {
            Map<String, DataSource> dbInfo = TenantDatasourceProvider.getDbInfo(defaultDataSource);
            if (!CollectionUtils.isEmpty(dbInfo)) {
                dynamicDataSourceMap.putAll(dbInfo);
            }
        }
        dynamicDataSource.setTargetDataSources(dynamicDataSourceMap);
        for (Map.Entry<Object, Object> entry : dynamicDataSourceMap.entrySet()) {
            Object key = entry.getKey();
            boolean isManage = key.equals(DataSourceConstant.DATA_SOURCE_MANAGE);
            try {
                TenantFlywayUtil.executeFlyway((HikariDataSource) entry.getValue(), isManage, appName);
            } catch (Exception e) {
                // 记录日志 循环继续
            }
        }
        //默认数据源
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;

    }

    /**
     * 从配置文件加载额外的数据源
     * DE官方库业务库可以从这里加载进去(客户业务库是从数据库读进去)
     *
     * @return
     */
    private Map<Object, Object> getDynamicDataSource() {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>(dataSourcePropertiesMap.size());
        dataSourcePropertiesMap.forEach((k, v) -> {
            HikariDataSource hikariDataSource = v.initializeDataSourceBuilder().type(HikariDataSource.class).build();
            targetDataSources.put(k, hikariDataSource);
        });
        return targetDataSources;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DSInterceptor());
    }
}
