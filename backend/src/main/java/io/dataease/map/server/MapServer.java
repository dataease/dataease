package io.dataease.map.server;

import io.dataease.map.api.MapApi;
import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.dto.request.MapNodeRequest;
import io.dataease.map.service.MapService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class MapServer implements MapApi {

    @Resource
    private MapService mapService;

    @Override
    public List<AreaEntity> areaEntitys(@PathVariable String pcode) {
        List<AreaEntity> areaEntities = mapService.areaEntities();
        if (StringUtils.equals(pcode, "0")) {
            return areaEntities;
        }
        return mapService.entitysByPid(areaEntities, pcode);
    }

    @Override
    public List<AreaEntity> globalEntitys(String pcode) {
        List<AreaEntity> areaEntities = mapService.globalEntities();
        if (StringUtils.equals(pcode, "0")) {
            return areaEntities;
        }
        return mapService.entitysByPid(areaEntities, pcode);
    }


    @Override
    public void saveMapNode(@RequestPart("request") MapNodeRequest request, @RequestPart(value = "file") MultipartFile file) throws Exception{
        mapService.saveMapNode(request, file);
    }

    @Override
    public void delMapNode(@RequestBody MapNodeRequest request) {
        mapService.delMapNode(request);
    }
}
