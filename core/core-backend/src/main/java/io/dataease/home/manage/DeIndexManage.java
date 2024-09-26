package io.dataease.home.manage;

import io.dataease.license.config.XpackInteract;
import org.springframework.stereotype.Component;

@Component
public class DeIndexManage {

    @XpackInteract(value = "deIndexManage", replace = true)
    public Boolean xpackModel() {
        return null;
    }
}
