package io.dataease.license.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class F2CLicense implements Serializable {

    @Serial
    private static final long serialVersionUID = -5264927171691944304L;

    private String corporation;
    private String isv;
    private String expired;
    private String licenseVersion;
    private String product;
    private Long generateTime;
    private String edition;
    private Long count;

    private String serialNo;
    private String remark;
}
