package io.dataease.map.server;

import io.dataease.commons.utils.LogUtil;
import io.dataease.map.api.MapApi;
import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.service.MapService;
import io.dataease.map.utils.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.ArrayList;
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
}
