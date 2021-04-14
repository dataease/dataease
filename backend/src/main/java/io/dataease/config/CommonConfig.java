package io.dataease.config;

import com.fit2cloud.autoconfigure.QuartzAutoConfiguration;
import io.dataease.commons.utils.CommonThreadPool;
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
                .config("spark.scheduler.mode", env.getProperty("spark.scheduler.mode", "FAIR"))
                .config("spark.serializer", env.getProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer"))
                .config("spark.executor.cores", env.getProperty("spark.executor.cores", "8"))
                .config("spark.executor.memory", env.getProperty("spark.executor.memory", "6442450944b"))
                .config("spark.locality.wait", env.getProperty("spark.locality.wait", "600000"))
                .config("spark.maxRemoteBlockSizeFetchToMem", env.getProperty("spark.maxRemoteBlockSizeFetchToMem", "2000m"))
                .config("spark.shuffle.detectCorrupt", env.getProperty("spark.shuffle.detectCorrupt", "false"))
                .config("spark.shuffle.service.enabled", env.getProperty("spark.shuffle.service.enabled", "true"))
                .config("spark.sql.adaptive.enabled", env.getProperty("spark.sql.adaptive.enabled", "true"))
                .config("spark.sql.adaptive.shuffle.targetPostShuffleInputSize", env.getProperty("spark.sql.adaptive.shuffle.targetPostShuffleInputSize", "200M"))
                .config("spark.sql.broadcastTimeout", env.getProperty("spark.sql.broadcastTimeout", "12000"))
                .config("spark.sql.retainGroupColumns", env.getProperty("spark.sql.retainGroupColumns", "false"))
                .config("spark.sql.sortMergeJoinExec.buffer.in.memory.threshold", env.getProperty("spark.sql.sortMergeJoinExec.buffer.in.memory.threshold", "100000"))
                .config("spark.sql.sortMergeJoinExec.buffer.spill.threshold", env.getProperty("spark.sql.sortMergeJoinExec.buffer.spill.threshold", "100000"))
                .config("spark.sql.variable.substitute", env.getProperty("spark.sql.variable.substitute", "false"))
                .config("spark.temp.expired.time", env.getProperty("spark.temp.expired.time", "3600"))
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
