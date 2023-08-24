package io.dataease.commons.license;

import lombok.Data;

@Data
public class F2CLicense {

    private String corporation;
    private String expired;
    private String licenseVersion;
    private String product;
    private Long generateTime;
    private String edition;
    private Long count;

    private String serialNo;
    private String remark;
}