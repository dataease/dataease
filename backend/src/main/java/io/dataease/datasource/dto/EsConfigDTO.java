package io.dataease.datasource.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsConfigDTO {
    private String url;
    private String esUsername;
    private String esPassword;
    private String version;
    private String uri;
    private String dataSourceType = "es";
}
