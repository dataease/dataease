package io.dataease.service.panel;

import io.dataease.base.mapper.ext.ExtPanelViewMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelViewDto;
import io.dataease.dto.panel.po.PanelViewPo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PanelViewService {

    @Autowired(required = false)
    private ExtPanelViewMapper extPanelViewMapper;

    private final static String SCENE_TYPE = "scene";

    public List<PanelViewPo> groups(){
        return extPanelViewMapper.groups(String.valueOf(AuthUtils.getUser().getUserId()));
    }

    public List<PanelViewPo> views(){
        return extPanelViewMapper.views(String.valueOf(AuthUtils.getUser().getUserId()));
    }

    public List<PanelViewDto> buildTree(List<PanelViewPo> groups, List<PanelViewPo> views){

        if (CollectionUtils.isEmpty(groups) || CollectionUtils.isEmpty(views)) return null;

        Map<String, List<PanelViewPo>> viewsMap = views.stream().collect(Collectors.groupingBy(PanelViewPo::getPid));
        List<PanelViewDto> dtos = groups.stream().map(group -> BeanUtils.copyBean(new PanelViewDto(), group)).collect(Collectors.toList());
        List<PanelViewDto> roots = new ArrayList<>();
        dtos.forEach(group -> {
            // 查找跟节点
            if (ObjectUtils.isEmpty(group.getPid())){
                roots.add(group);
            }
            // 查找当前节点的子节点
            // 当前group是场景
            if (StringUtils.equals(group.getType(), SCENE_TYPE)){
                Optional.ofNullable(viewsMap.get(group.getId())).ifPresent(lists -> lists.forEach(view -> {
                    PanelViewDto dto = BeanUtils.copyBean(new PanelViewDto(), view);
                    group.addChild(dto);
                }));
                return;
            }
            // 当前group是分组
            dtos.forEach(item -> {
                if (StringUtils.equals(item.getPid(), group.getId())){
                    group.addChild(item);
                }
            });
        });
        // 最后 没有孩子的老东西淘汰
        return roots.stream().filter(item -> CollectionUtils.isNotEmpty(item.getChildren())).collect(Collectors.toList());
    }
}
