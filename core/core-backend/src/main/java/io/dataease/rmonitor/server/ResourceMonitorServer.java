package io.dataease.rmonitor.server;

import io.dataease.api.rmonitor.ResourceMonitorApi;
import io.dataease.rmonitor.manage.ResourceMonitorManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rmonitor")
public class ResourceMonitorServer implements ResourceMonitorApi {

    @Resource(name = "resourceMonitorManage")
    private ResourceMonitorManage resourceMonitorManage;

    @Override
    public boolean existFree() {
        return resourceMonitorManage.check();
    }

    @Override
    public void delete() {
        resourceMonitorManage.delete();
    }

    @Override
    public void sync() {
        resourceMonitorManage.sync();
    }
}
