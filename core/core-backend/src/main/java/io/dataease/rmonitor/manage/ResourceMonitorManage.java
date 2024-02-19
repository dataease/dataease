package io.dataease.rmonitor.manage;

import io.dataease.exception.DEException;
import io.dataease.rmonitor.bo.PerMonitorCheckBO;
import io.dataease.rmonitor.bo.PerMonitorNodeBO;
import io.dataease.rmonitor.mapper.ResourceMonitorMapper;
import io.dataease.rmonitor.mapper.entity.DatasetFreeResource;
import io.dataease.rmonitor.mapper.entity.DsFreeResource;
import io.dataease.rmonitor.mapper.entity.VisualFreeResource;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("resourceMonitorManage")
public class ResourceMonitorManage {


    @Resource(name = "resourceMonitorSyncManage")
    private ResourceMonitorSyncManage resourceMonitorSyncManage;

    @Resource
    private ResourceMonitorMapper resourceMonitorMapper;


    private boolean existFreeResource() {
        int rCount = resourceMonitorMapper.dsCount() + resourceMonitorMapper.datasetCount() + resourceMonitorMapper.vCount();
        return rCount > 0;
    }

    private Map<String, List<PerMonitorNodeBO>> freeResource() {
        Map<String, List<PerMonitorNodeBO>> result = new HashMap<>();

        List<DsFreeResource> dsFreeResources = resourceMonitorMapper.queryFreeDs();
        if (CollectionUtils.isNotEmpty(dsFreeResources)) {
            List<PerMonitorNodeBO> dsBos = dsFreeResources.stream().map(node -> {
                PerMonitorNodeBO bo = BeanUtils.copyBean(new PerMonitorNodeBO(), node);
                bo.setLeaf(!StringUtils.equals("folder", node.getType()));
                return bo;
            }).collect(Collectors.toList());
            List<PerMonitorNodeBO> dsTree = TreeUtils.mergeTree(dsBos, PerMonitorNodeBO.class, false);
            result.put("datasource", dsTree);
        }

        List<DatasetFreeResource> datasetFreeResources = resourceMonitorMapper.queryFreeDataset();
        if (CollectionUtils.isNotEmpty(datasetFreeResources)) {
            List<PerMonitorNodeBO> datasetBos = datasetFreeResources.stream().map(node -> {
                PerMonitorNodeBO bo = BeanUtils.copyBean(new PerMonitorNodeBO(), node);
                bo.setLeaf(!StringUtils.equals("folder", node.getNodeType()));
                return bo;
            }).collect(Collectors.toList());
            List<PerMonitorNodeBO> datasetTree = TreeUtils.mergeTree(datasetBos, PerMonitorNodeBO.class, false);
            result.put("dataset", datasetTree);
        }

        List<VisualFreeResource> visualFreeResources = resourceMonitorMapper.queryFreeVusial();
        if (CollectionUtils.isNotEmpty(visualFreeResources)) {
            Map<String, List<VisualFreeResource>> baseMap = visualFreeResources.stream().collect(Collectors.groupingBy(VisualFreeResource::getType));
            for (Map.Entry<String, List<VisualFreeResource>> entry : baseMap.entrySet()) {
                List<VisualFreeResource> freeResource = entry.getValue();
                List<PerMonitorNodeBO> visualBos = freeResource.stream().map(node -> {
                    PerMonitorNodeBO bo = BeanUtils.copyBean(new PerMonitorNodeBO(), node);
                    bo.setLeaf(!StringUtils.equals("folder", node.getNodeType()));
                    return bo;
                }).collect(Collectors.toList());
                result.put(convertBusiFlag(entry.getKey()), TreeUtils.mergeTree(visualBos, PerMonitorNodeBO.class, false));
            }
        }
        return result;
    }

    private String convertBusiFlag(String key) {
        if (StringUtils.equals("dashboard", key)){
            return "panel";
        } else if (StringUtils.equals("dataV", key)) {
            return "screen";
        } else return key;
    }

    public boolean check() {
        PerMonitorCheckBO checkBO = resourceMonitorSyncManage.checkXpackResource();
        return checkBO.isValid() && checkBO.isEmptyPermission() && existFreeResource();
    }

    @Transactional
    public void delete() {
        boolean existFree = existFreeResource();
        if (!existFree) DEException.throwException("无未同步资源！");
        resourceMonitorMapper.delFreeDs();
        resourceMonitorMapper.delFreeDataset();
        resourceMonitorMapper.delFreeVisual();
    }

    public void sync() {
        //1、从xpack获取资源 如果xpack不存在 或者资源不为空 则直接返回 并且抛出异常“仅支持首次导入lic同步”
        //2、从core获取资源
        //3、根据类型分组 并组织成树形结构
        //4、分别遍历每一棵树 从上到下 同步到权限体系 给默认组织
        PerMonitorCheckBO checkBO = resourceMonitorSyncManage.checkXpackResource();
        if (!checkBO.isValid()) DEException.throwException("缺少许可证");
        if (!checkBO.isEmptyPermission()) DEException.throwException("仅支持license首次导入同步");
        Map<String, List<PerMonitorNodeBO>> freeResourceMap = freeResource();
        if (MapUtils.isEmpty(freeResourceMap)) DEException.throwException("无未同步资源！");
        for (Map.Entry<String, List<PerMonitorNodeBO>> entry : freeResourceMap.entrySet()) {
            resourceMonitorSyncManage.sync(entry.getKey(), entry.getValue());
        }
    }

}
