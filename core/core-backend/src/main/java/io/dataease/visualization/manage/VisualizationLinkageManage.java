package io.dataease.visualization.manage;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.visualization.dto.LinkageInfoDTO;
import io.dataease.api.visualization.dto.VisualizationLinkageDTO;
import io.dataease.api.visualization.vo.VisualizationLinkageFieldVO;
import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.dao.auto.repo.CoreDatasetTableFieldRepository;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class VisualizationLinkageManage {
    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private VisualizationLinkageRepository linkageRepository;

    @Resource
    private VisualizationLinkageFieldRepository linkageFieldRepository;

    @Resource
    private CoreChartViewRepository coreChartViewRepository;

    @Resource
    private CoreDatasetTableFieldRepository coreDatasetTableFieldRepository;



    @Resource
    private SnapshotCoreChartViewRepository snapshotCoreChartViewRepository;

    @Resource
    private SnapshotVisualizationLinkageRepository snapshotLinkageRepository;

    @Resource
    private SnapshotVisualizationLinkageFieldRepository snapshotVisualizationLinkageFieldRepository;


    public void copyLinkage(Long copyId) {
        // 1. 查询视图映射关系（源视图ID -> 目标视图ID）
        QCoreChartView pvs = QCoreChartView.coreChartView;
        QCoreChartView pvt = QCoreChartView.coreChartView;

        List<Tuple> viewMappings = queryFactory
                .select(
                        pvs.sceneId.as("sDvId"),
                        pvs.id.as("sChartViewId"),
                        pvt.sceneId.as("tDvId"),
                        pvt.id.as("tChartViewId"))
                .from(pvt)
                .innerJoin(pvs).on(pvt.copyFrom.eq(pvs.id))
                .where(pvt.copyId.eq(copyId))
                .fetch();

        if (viewMappings.isEmpty()) {
            return;
        }

        // 2. 构建视图映射关系（使用Map嵌套Map）
        Map<Long, Map<Long, Long>> sourceViewToTargetView = viewMappings.stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(0, Long.class), // sDvId
                        Collectors.toMap(
                                t -> t.get(1, Long.class), // sChartViewId
                                t -> t.get(3, Long.class)  // tChartViewId
                        )
                ));

        // 3. 查询需要复制的源linkage记录
        List<Long> sourceDvIds = viewMappings.stream()
                .map(t -> t.get(0, Long.class))
                .distinct()
                .collect(Collectors.toList());

        List<VisualizationLinkage> sourceLinkages = queryFactory
                .selectFrom(QVisualizationLinkage.visualizationLinkage)
                .where(QVisualizationLinkage.visualizationLinkage.dvId.in(sourceDvIds))
                .fetch();

        // 4. 创建新记录并批量保存
        List<VisualizationLinkage> newLinkages = sourceLinkages.stream()
                .map(source -> {
                    Map<Long, Long> viewMap = sourceViewToTargetView.get(source.getDvId());
                    if (viewMap != null) {
                        Long newSourceViewId = viewMap.get(source.getSourceViewId());
                        Long newTargetViewId = viewMap.get(source.getTargetViewId());

                        if (newSourceViewId != null && newTargetViewId != null) {
                            VisualizationLinkage newLinkage = new VisualizationLinkage();
                            newLinkage.setId(source.getId() + copyId);
                            newLinkage.setDvId(source.getDvId()); // 保持相同dvId
                            newLinkage.setSourceViewId(newSourceViewId);
                            newLinkage.setTargetViewId(newTargetViewId);
                            newLinkage.setUpdateTime(source.getUpdateTime());
                            newLinkage.setUpdatePeople(source.getUpdatePeople());
                            newLinkage.setLinkageActive(source.getLinkageActive());
                            newLinkage.setExt1(source.getExt1());
                            newLinkage.setExt2(source.getExt2());
                            newLinkage.setCopyFrom(source.getId());
                            newLinkage.setCopyId(copyId);
                            return newLinkage;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. 批量保存
        linkageRepository.saveAll(newLinkages);
    }


    public void copyLinkageField(Long copyId) {
        // 1. 查询需要复制的映射关系（linkage_id 映射）
        QVisualizationLinkage linkage = QVisualizationLinkage.visualizationLinkage;

        List<Tuple> linkageMappings = queryFactory
                .select(
                        linkage.id.as("newLinkageId"),
                        linkage.copyFrom.as("originalLinkageId"))
                .from(linkage)
                .where(linkage.copyId.eq(copyId))
                .fetch();

        if (linkageMappings.isEmpty()) {
            return;
        }

        // 2. 构建ID映射Map
        Map<Long, Long> linkageIdMap = linkageMappings.stream()
                .collect(Collectors.toMap(
                        t -> t.get(1, Long.class),  // originalLinkageId
                        t -> t.get(0, Long.class),  // newLinkageId
                        (existing, replacement) -> existing
                ));

        // 3. 查询需要复制的源linkageField记录
        List<VisualizationLinkageField> sourceFields = queryFactory
                .selectFrom(QVisualizationLinkageField.visualizationLinkageField)
                .where(QVisualizationLinkageField.visualizationLinkageField
                        .linkageId.in(linkageIdMap.keySet()))
                .fetch();

        // 4. 创建新记录并批量保存
        List<VisualizationLinkageField> newFields = sourceFields.stream()
                .map(source -> {
                    Long newLinkageId = linkageIdMap.get(source.getLinkageId());
                    if (newLinkageId != null) {
                        VisualizationLinkageField newField = new VisualizationLinkageField();
                        newField.setId(source.getId() + copyId);
                        newField.setLinkageId(newLinkageId);
                        newField.setSourceField(source.getSourceField());
                        newField.setTargetField(source.getTargetField());
                        newField.setUpdateTime(source.getUpdateTime());
                        newField.setCopyFrom(source.getId());
                        newField.setCopyId(copyId);
                        return newField;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. 批量保存
        linkageFieldRepository.saveAll(newFields);
    }

    public List<VisualizationLinkageDTO> getViewLinkageGatherSnapshot(Long dvId, Long sourceViewId, List<String> targetViewIds) {
        // 转换 targetViewIds 为 Long 类型
        List<Long> targetViewIdList = targetViewIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 1. 获取目标视图信息
        List<SnapshotCoreChartView> targetViews = snapshotCoreChartViewRepository.findByIdInAndTypeNot(targetViewIdList, "VQuery");

        // 2. 获取联动信息
        List<SnapshotVisualizationLinkage> linkages = snapshotLinkageRepository.findByDvIdAndSourceViewId(dvId, sourceViewId);

        // 3. 获取联动字段信息
        List<Long> linkageIds = linkages.stream()
                .map(SnapshotVisualizationLinkage::getId)
                .collect(Collectors.toList());
        List<SnapshotVisualizationLinkageField> linkageFields = snapshotVisualizationLinkageFieldRepository.findByLinkageIdIn(linkageIds);

        // 4. 构建结果
        return targetViews.stream().map(targetView -> {
            VisualizationLinkageDTO dto = new VisualizationLinkageDTO();
            dto.setTargetViewId(targetView.getId());
            dto.setTargetViewType(targetView.getType());
            dto.setTableId(targetView.getTableId());
            dto.setTargetViewName(targetView.getTitle());
            dto.setSourceViewId(sourceViewId);

            // 设置联动状态
            linkages.stream()
                    .filter(linkage -> linkage.getTargetViewId().equals(targetView.getId()))
                    .findFirst()
                    .ifPresent(linkage -> {
                        dto.setLinkageActive(linkage.getLinkageActive());
                    });

            // 设置联动字段
            List<VisualizationLinkageFieldVO> fieldVOs = linkageFields.stream()
                    .filter(field -> linkages.stream()
                            .anyMatch(linkage ->
                                    linkage.getId().equals(field.getLinkageId()) &&
                                            linkage.getTargetViewId().equals(targetView.getId())
                            ))
                    .map(field -> {
                        VisualizationLinkageFieldVO vo = new VisualizationLinkageFieldVO();
                        vo.setSourceField(field.getSourceField());
                        vo.setTargetField(field.getTargetField());
                        return vo;
                    })
                    .collect(Collectors.toList());
            dto.setLinkageFields(fieldVOs);

            // 设置目标视图字段
            if (targetView.getTableId() != null) {
                List<DatasetTableFieldDTO> fields = coreDatasetTableFieldRepository.findByDatasetTableId(targetView.getTableId())
                        .stream()
                        .map(field -> {
                            DatasetTableFieldDTO fieldDto = new DatasetTableFieldDTO();
                            fieldDto.setId(field.getId());
                            fieldDto.setDatasetTableId(field.getDatasetTableId());
                            fieldDto.setOriginName(field.getOriginName());
                            fieldDto.setName(field.getName());
                            fieldDto.setDeType(field.getDeType());
                            return fieldDto;
                        })
                        .collect(Collectors.toList());
                dto.setTargetViewFields(fields);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public List<VisualizationLinkageDTO> getViewLinkageGather(Long dvId, Long sourceViewId, List<String> targetViewIds) {
        // 转换 targetViewIds 为 Long 类型
        List<Long> targetViewIdList = targetViewIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 1. 获取目标视图信息
        List<CoreChartView> targetViews = coreChartViewRepository.findByIdInAndTypeNot(targetViewIdList, "VQuery");

        // 2. 获取联动信息
        List<VisualizationLinkage> linkages = linkageRepository.findByDvIdAndSourceViewId(dvId, sourceViewId);

        // 3. 获取联动字段信息
        List<Long> linkageIds = linkages.stream()
                .map(VisualizationLinkage::getId)
                .collect(Collectors.toList());
        List<VisualizationLinkageField> linkageFields = linkageFieldRepository.findByLinkageIdIn(linkageIds);

        // 4. 构建结果
        return targetViews.stream().map(targetView -> {
            VisualizationLinkageDTO dto = new VisualizationLinkageDTO();
            dto.setTargetViewId(targetView.getId());
            dto.setTargetViewType(targetView.getType());
            dto.setTableId(targetView.getTableId());
            dto.setTargetViewName(targetView.getTitle());
            dto.setSourceViewId(sourceViewId);

            // 设置联动状态
            linkages.stream()
                    .filter(linkage -> linkage.getTargetViewId().equals(targetView.getId()))
                    .findFirst()
                    .ifPresent(linkage -> {
                        dto.setLinkageActive(linkage.getLinkageActive());
                    });

            // 设置联动字段
            List<VisualizationLinkageFieldVO> fieldVOs = linkageFields.stream()
                    .filter(field -> linkages.stream()
                            .anyMatch(linkage ->
                                    linkage.getId().equals(field.getLinkageId()) &&
                                            linkage.getTargetViewId().equals(targetView.getId())
                            ))
                    .map(field -> {
                                VisualizationLinkageFieldVO vo = new VisualizationLinkageFieldVO();
                                vo.setSourceField(field.getSourceField());
                                vo.setTargetField(field.getTargetField());
                                return vo;
                            })
                    .collect(Collectors.toList());
            dto.setLinkageFields(fieldVOs);

            // 设置目标视图字段
            if (targetView.getTableId() != null) {
                List<DatasetTableFieldDTO> fields = coreDatasetTableFieldRepository.findByDatasetTableId(targetView.getTableId())
                        .stream()
                        .map(field -> {
                            DatasetTableFieldDTO fieldDto = new DatasetTableFieldDTO();
                            fieldDto.setId(field.getId());
                            fieldDto.setDatasetTableId(field.getDatasetTableId());
                            fieldDto.setOriginName(field.getOriginName());
                            fieldDto.setName(field.getName());
                            fieldDto.setDeType(field.getDeType());
                            return fieldDto;
                        })
                        .collect(Collectors.toList());
                dto.setTargetViewFields(fields);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public List<LinkageInfoDTO> getPanelAllLinkageInfo(Long dvId) {
        // 1. 查询符合条件的联动信息
        List<VisualizationLinkage> linkages = linkageRepository.findByDvIdAndLinkageActive(dvId, true);

        // 2. 创建分组Map
        Map<String, List<String>> groupedResults = new HashMap<>();

        // 3. 填充分组数据
        linkages.stream()
                .filter(linkage -> linkage.getSourceView() != null && linkage.getSourceView().getLinkageActive())
                .forEach(linkage -> {
                    linkage.getLinkageFields().stream()
                            .filter(field -> field.getId() != null)
                            .forEach(field -> {
                                String sourceKey = linkage.getSourceViewId() + "#" + field.getSourceField();
                                String targetValue = linkage.getTargetViewId() + "#" + field.getTargetField();
                                groupedResults.computeIfAbsent(sourceKey, k -> new ArrayList<>()).add(targetValue);
                            });
                });

        // 4. 转换为 LinkageInfoDTO 列表
        return groupedResults.entrySet().stream()
                .map(entry -> {
                    LinkageInfoDTO dto = new LinkageInfoDTO();
                    dto.setSourceInfo(entry.getKey());
                    dto.setTargetInfoList(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<LinkageInfoDTO> getPanelAllLinkageInfoSnapshot(Long dvId) {
        return null;
    }
}
