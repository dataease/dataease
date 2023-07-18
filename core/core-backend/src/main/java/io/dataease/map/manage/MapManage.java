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

    @Cacheable(value = "world_map", key = "'world_map'")
    public AreaNode getWorldTree() {
        List<Area> areas = areaMapper.selectList(null);
        WORLD.setChildren(new ArrayList<>());
        var areaNodeMap = new HashMap<String, AreaNode>();
        areaNodeMap.put(WORLD.getId(), WORLD);
        areas.forEach(area -> {
//            当前节点不存在就添加，已存在就是子节点添加的，拷贝一下属性
            var node = areaNodeMap.get(area.getId());
            if (node == null) {
                node = AreaNode.builder().build();
                BeanUtils.copyBean(node, area);
                areaNodeMap.put(area.getId(), node);
            } else {
                BeanUtils.copyBean(node, area);
            }
//            检查父节点是否存在，不存在就添加然后把自己加到子节点，已存在就加到子节点
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
