package io.dataease.config;

import com.github.pagehelper.PageInterceptor;
import io.dataease.commons.utils.CompressUtils;
import io.dataease.commons.utils.MybatisInterceptorConfig;
import io.dataease.interceptor.MybatisInterceptor;
import io.dataease.plugins.common.base.domain.AuthSource;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.FileContent;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
@MapperScan(basePackages = {"io.dataease.ext", "io.dataease.plugins"}, sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class MybatisConfig {

    @Bean
    @ConditionalOnMissingBean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("pageSizeZero", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        configList.add(new MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(Datasource.class, "configuration"));
        configList.add(new MybatisInterceptorConfig(AuthSource.class, "configuration"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }
}
