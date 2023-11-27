package io.dataease.map.server;

import io.dataease.api.map.GeoApi;
import io.dataease.api.map.dto.GeometryNodeCreator;
import io.dataease.map.manage.MapManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/geometry")
public class GeoServer implements GeoApi {

    @Resource
    private MapManage mapManage;
    @Override
    public void saveMapGeo(GeometryNodeCreator request, MultipartFile file) {
        mapManage.saveMapGeo(request, file);
    }

    @Override
    public void deleteGeo(String id) {
        mapManage.deleteGeo(id);
    }
}
