package io.dataease.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "dataease")
@Data
public class WhitelistConfig {

    private List<String> whitelist;


}
