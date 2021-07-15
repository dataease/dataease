package io.dataease.map.service;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.file.FileReader;
import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.utils.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapService {


    private static final String dirPath = "/opt/dataease/data/feature/";

    // 要不要加缓存呢？
    public String geometry(String areaCode) {
        String path = dirPath + "full/" + areaCode + "_full.json";
        FileReader fileReader = new FileReader(path);
        return fileReader.readString();
    }

    @Cacheable("sys_map_areas")
    public List<AreaEntity> areaEntities() {
        List<AreaEntity> areaEntities = MapUtils.readAreaEntity();
        return areaEntities;
    }

    public List<AreaEntity> entitysByPid(List<AreaEntity> entities, String pid) {
        for (int i = 0; i < entities.size(); i++) {
            AreaEntity areaEntity = entities.get(i);
            if (StringUtils.equals(pid, areaEntity.getCode())) {
                return areaEntity.getChildren();
            }

            if (CollectionUtil.isNotEmpty(areaEntity.getChildren())) {
                List<AreaEntity> areaEntities = entitysByPid(areaEntity.getChildren(), pid);
                if (null != areaEntities){
                    return areaEntities;
                }
            }
        }
        return null;

    }



}
