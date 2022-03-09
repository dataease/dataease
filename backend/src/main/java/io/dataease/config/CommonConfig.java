package io.dataease.config;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.autoconfigure.QuartzAutoConfiguration;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.utils.CommonThreadPool;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@Configuration
@AutoConfigureBefore(QuartzAutoConfiguration.class)
public class CommonConfig {

    @Resource
    private Environment env; // 保存了配置文件的信息
    private static String root_path = "/opt/dataease/data/kettle/";

    @Bean
    @ConditionalOnMissingBean
    public KettleFileRepository kettleFileRepository() throws Exception {
        KettleEnvironment.init();
        KettleFileRepository repository = new KettleFileRepository();
        KettleFileRepositoryMeta kettleDatabaseMeta = new KettleFileRepositoryMeta("KettleFileRepository", "repo",
                "dataease kettle repo", root_path);
        repository.init(kettleDatabaseMeta);
        return repository;
    }

    @Bean(destroyMethod = "shutdown")
    public CommonThreadPool resourcePoolThreadPool() {
        CommonThreadPool commonThreadPool = new CommonThreadPool();
        commonThreadPool.setCorePoolSize(20);
        commonThreadPool.setMaxQueueSize(100);
        commonThreadPool.setKeepAliveSeconds(3600);
        return commonThreadPool;
    }
}
