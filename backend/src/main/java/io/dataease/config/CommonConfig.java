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

    @Bean(name = "DorisDatasource")
    @ConditionalOnMissingBean
    public Datasource configuration() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dataSourceType", "jdbc");
        jsonObject.put("dataBase", env.getProperty("doris.db", "doris"));
        jsonObject.put("username", env.getProperty("doris.user", "root"));
        jsonObject.put("password", env.getProperty("doris.password", "dataease"));
        jsonObject.put("host", env.getProperty("doris.host", "doris"));
        jsonObject.put("port", env.getProperty("doris.port", "9030"));
        jsonObject.put("httpPort", env.getProperty("doris.httpPort", "8030"));

        Datasource datasource = new Datasource();
        datasource.setId("doris");
        datasource.setName("doris");
        datasource.setDesc("doris");
        datasource.setType("de_doris");
        datasource.setConfiguration(jsonObject.toJSONString());
        return datasource;
    }


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
