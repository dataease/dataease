package io.dataease.map.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.map.vo.AreaNode;
import io.dataease.map.dao.auto.entity.Area;
import io.dataease.map.dao.auto.mapper.AreaMapper;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.dataease.constant.CacheConstant.CommonCacheConstant.WORLD_MAP_CACHE;

@Component
public class MapManage {
    private final static AreaNode WORLD;

    static {
        WORLD = AreaNode.builder()
                .id("000")
                .level("world")
                .name("世界村")
                .build();
    }

    @Resource
    private AreaMapper areaMapper;

    @Cacheable(value = WORLD_MAP_CACHE, key = "'world_map'")
    public AreaNode getWorldTree() {
        List<Area> areas = areaMapper.selectList(null);
        WORLD.setChildren(new ArrayList<>());
        var areaNodeMap = new HashMap<String, AreaNode>();
        areaNodeMap.put(WORLD.getId(), WORLD);
        areas.forEach(area -> {
            var node = areaNodeMap.get(area.getId());
            if (node == null) {
                node = AreaNode.builder().build();
                BeanUtils.copyBean(node, area);
                areaNodeMap.put(area.getId(), node);
            } else {
                BeanUtils.copyBean(node, area);
            }
            var pNode = areaNodeMap.get(area.getPid());
            if (pNode == null) {
                var child = new ArrayList<AreaNode>();
                child.add(node);
                pNode = AreaNode.builder()
                        .children(child)
                        .id(area.getPid())
                        .build();
                areaNodeMap.put(area.getPid(), pNode);
            } else {
                if (pNode.getChildren() == null) {
                    pNode.setChildren(new ArrayList<>());
                }
                pNode.getChildren().add(node);
            }
        });
        return WORLD;
    }


}
