package io.dataease.map.server;

import io.dataease.api.map.MapApi;
import io.dataease.api.map.vo.AreaNode;
import io.dataease.map.manage.MapManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/map")
public class MapServer implements MapApi {
    @Resource
    private MapManage mapManage;

    @Override
    public AreaNode getWorldTree() {
        return mapManage.getWorldTree();
    }
}
