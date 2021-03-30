package io.dataease.config;

import com.fit2cloud.autoconfigure.QuartzAutoConfiguration;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
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
    public JavaSparkContext javaSparkContext() {
        SparkSession spark = SparkSession.builder()
                .appName(env.getProperty("spark.appName", "DataeaseJob"))
                .master(env.getProperty("spark.master", "local[*]"))
                .getOrCreate();
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        return sc;
    }

    @Bean
    @ConditionalOnMissingBean
    public SQLContext sqlContext(JavaSparkContext javaSparkContext) {
        SQLContext sqlContext = new SQLContext(javaSparkContext);
        sqlContext.setConf("spark.sql.shuffle.partitions", env.getProperty("spark.sql.shuffle.partitions", "1"));
        sqlContext.setConf("spark.default.parallelism", env.getProperty("spark.default.parallelism", "1"));
        return sqlContext;
    }
}
