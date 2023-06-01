package io.dataease.license.manage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoreLicManage {

    @Value("${dataease.version}")
    private String version;

    public String getVersion() {
        return version;
    }

}
