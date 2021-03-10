package io.dataease.config;

import com.fit2cloud.autoconfigure.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@Configuration
@AutoConfigureBefore(QuartzAutoConfiguration.class)
public class HbaseConfig {

    @Resource
    private Environment env; // 保存了配置文件的信息


    @Bean
    @ConditionalOnMissingBean
    public org.apache.hadoop.conf.Configuration configuration(){
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("hbase.zookeeper.quorum", env.getProperty("hbase.zookeeper.quorum"));
        configuration.set("hbase.zookeeper.property.clientPort", env.getProperty("hbase.zookeeper.property.clientPort"));
        configuration.set("hbase.client.retries.number", env.getProperty("hbase.client.retries.number", "1"));
        return configuration;
    }
}
