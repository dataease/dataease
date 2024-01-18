package io.dataease.rmonitor.manage;

import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.rmonitor.bo.PerMonitorCheckBO;
import io.dataease.rmonitor.bo.PerMonitorNodeBO;
import io.dataease.rmonitor.mapper.ResourceMonitorMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("resourceMonitorSyncManage")
public class ResourceMonitorSyncManage {

    @Resource(name = "resourceMonitorMapper")
    private ResourceMonitorMapper resourceMonitorMapper;

    @XpackInteract(value = "resourceMonitorSyncManage", replace = true)
    public void sync(String flag, List<PerMonitorNodeBO> treeNodes) {
        DEException.throwException("缺失许可证");
    }

    @XpackInteract(value = "resourceMonitorSyncManage", replace = true)
    public PerMonitorCheckBO checkXpackResource() {
        return new PerMonitorCheckBO();
    }
}
