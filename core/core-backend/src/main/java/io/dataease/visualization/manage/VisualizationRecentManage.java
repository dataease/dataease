package io.dataease.visualization.manage;


import com.alibaba.excel.util.StringUtils;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.operation.dao.auto.entity.CoreOptRecent;
import io.dataease.operation.dao.auto.mapper.CoreOptRecentRepository;
import io.dataease.visualization.dao.auto.entity.CoreStore;
import io.dataease.visualization.dao.auto.mapper.CoreStoreRepository;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.ext.po.VisualizationResourcePO;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class VisualizationRecentManage {

    @Resource
    private CoreDatasetGroupRepository datasetGroupRepository;
    @Resource
    private CoreDatasourceRepository datasourceRepository;
    @Resource
    private DataVisualizationInfoRepository dataVizRepository;
    @Resource
    private CoreOptRecentRepository optRecentRepository;
    @Resource
    private CoreStoreRepository storeRepository;

    public Page<VisualizationResourcePO> findRecent(Long uid, String keyword, Map<String, Object> params, Pageable pageable) {
        // 1. 获取用户收藏的资源ID集合
        Specification<CoreStore> findStoredResourceIdsByUserSpec = (root, query, cb) ->
                cb.equal(root.get("uid"), uid);
        Set<Long> storedResourceIds = storeRepository.findAll(findStoredResourceIdsByUserSpec)
                .stream()
                .map(CoreStore::getResourceId)
                .collect(Collectors.toSet());

        // 2. 获取用户最近操作的资源
        List<CoreOptRecent> recentOperations = optRecentRepository.findByUid(uid);

        // 3. 查询各类资源
        List<VisualizationResourcePO> datasetResources = queryDatasets(uid, keyword, params, storedResourceIds);
        List<VisualizationResourcePO> datasourceResources = queryDatasources(uid, keyword, params, storedResourceIds);
        List<VisualizationResourcePO> vizResources = queryVisualizations(uid, keyword, params, storedResourceIds);

        // 4. 合并结果并关联最近操作时间
        List<VisualizationResourcePO> allResources = new ArrayList<>();
        allResources.addAll(datasetResources);
        allResources.addAll(datasourceResources);
        allResources.addAll(vizResources);

        // 5. 关联最近操作时间
        Map<Long, Long> resourceRecentTimeMap = recentOperations.stream()
                .collect(Collectors.toMap(
                        CoreOptRecent::getResourceId,
                        CoreOptRecent::getTime,
                        (existing, replacement) -> existing > replacement ? existing : replacement
                ));

        allResources.forEach(resource -> {
            resource.setLastEditTime(resourceRecentTimeMap.get(resource.getResourceId()));
            resource.setFavorite(storedResourceIds.contains(resource.getResourceId()));
        });

        // 6. 过滤和排序
        List<VisualizationResourcePO> filteredResources = applyFilters(allResources, keyword, params);

        // 7. 分页处理
        return paginateResults(filteredResources, pageable, params);
    }

    private List<VisualizationResourcePO> queryDatasets(Long uid, String keyword, Map<String, Object> params, Set<Long> storedResourceIds) {
        return datasetGroupRepository.findByNodeType("dataset")
                .stream()
                .map(ds -> new VisualizationResourcePO(
                        ds.getId(),
                        ds.getId(),
                        ds.getName(),
                        "dataset",
                        0L,
                        0L,
                        ds.getLastUpdateTime(),
                        false, 0, 0
                ))
                .collect(Collectors.toList());
    }

    private List<VisualizationResourcePO> queryDatasources(Long uid, String keyword, Map<String, Object> params, Set<Long> storedResourceIds) {
        return datasourceRepository.findByTypeNot("folder")
                .stream()
                .map(ds -> new VisualizationResourcePO(
                        ds.getId(),
                        ds.getId(),
                        ds.getName(),
                        "datasource",
                        0L,
                        0L,
                        ds.getUpdateTime(),
                        false, 0, 0
                ))
                .collect(Collectors.toList());
    }

    private List<VisualizationResourcePO> queryVisualizations(Long uid, String keyword, Map<String, Object> params, Set<Long> storedResourceIds) {
        return dataVizRepository.findByDeleteFlagAndNodeTypeAndStatusNot(false, "leaf", 0)
                .stream()
                .map(dv -> new VisualizationResourcePO(
                        dv.getId(),
                        dv.getId(),
                        dv.getName(),
                        "dataV".equals(dv.getType()) ? "screen" : "panel",
                        0L,
                        0L,
                        dv.getUpdateTime(),
                        false, 0, 0
                ))
                .collect(Collectors.toList());
    }

    private List<VisualizationResourcePO> applyFilters(List<VisualizationResourcePO> resources, String keyword, Map<String, Object> params) {
        Stream<VisualizationResourcePO> stream = resources.stream();

        if (StringUtils.isNotBlank(keyword)) {
            String lowerKeyword = keyword.toLowerCase();
            stream = stream.filter(r -> r.getName().toLowerCase().contains(lowerKeyword));
        }

        if (params.get("type") != null) {
            String type = params.get("type").toString();
            stream = stream.filter(r -> type.equals(r.getType()));
        }

        if (params.get("info") != null) {
            // 处理NOT EXISTS逻辑
        }

        return stream.collect(Collectors.toList());
    }

    private Page<VisualizationResourcePO> paginateResults(List<VisualizationResourcePO> resources, Pageable pageable, Map<String, Object> params) {
        // 排序
        if (Boolean.TRUE.equals(params.get("isAsc"))) {
            resources.sort(Comparator.comparing(VisualizationResourcePO::getLastEditTime));
        } else {
            resources.sort(Comparator.comparing(VisualizationResourcePO::getLastEditTime).reversed());
        }

        // 分页
        int total = resources.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        return new PageImpl<>(resources.subList(start, end), pageable, total);
    }
}
