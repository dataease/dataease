package io.dataease.config;

import com.fit2cloud.autoconfigure.QuartzAutoConfiguration;
import io.dataease.commons.utils.CommonThreadPool;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
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
    public org.apache.hadoop.conf.Configuration configuration() {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("hbase.zookeeper.quorum", env.getProperty("hbase.zookeeper.quorum"));
        configuration.set("hbase.zookeeper.property.clientPort", env.getProperty("hbase.zookeeper.property.clientPort"));
        configuration.set("hbase.client.retries.number", env.getProperty("hbase.client.retries.number", "1"));
        return configuration;
    }

    @Bean
    @ConditionalOnMissingBean
    public SparkSession javaSparkSession() {
        SparkSession spark = SparkSession.builder()
                .appName(env.getProperty("spark.appName", "DataeaseJob"))
                .master(env.getProperty("spark.master", "local[*]"))
                .config("spark.scheduler.mode", "FAIR")
                .getOrCreate();
        return spark;
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
