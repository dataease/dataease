package io.dataease.map.server;

import io.dataease.commons.utils.LogUtil;
import io.dataease.map.api.MapApi;
import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.service.MapService;
import io.dataease.map.utils.MapUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@RestController
public class MapServer implements MapApi {



    @Resource
    private MapService mapService;

    @Override
    public String resourceFull(@PathVariable String areaCode) {
        return mapService.geometry(areaCode);
    }

    @Override
    public String asyncGeometry() {
        try {
            // List<AreaEntity> areaEntities = MapUtils.readAreaEntity();
            List<AreaEntity> areaEntities = mapService.areaEntities();
            MapUtils.recursionWriteFull(areaEntities);
        }catch (Exception e) {
            LogUtil.error(e);
            return e.getMessage();
        }
        return "async success";
    }

    @Override
    public List<AreaEntity> areaEntitys(@PathVariable String pcode) {
        List<AreaEntity> areaEntities = mapService.areaEntities();

        return mapService.entitysByPid(areaEntities, pcode);

        /*return areaEntities.stream().filter(item -> StringUtils.equals(item.getPcode(), pcode)).map(item -> {
            item.setChildren(null);
            return item;
        }).collect(Collectors.toList());*/
    }
}
