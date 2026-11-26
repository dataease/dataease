package io.dataease.visualization.manage;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.visualization.dto.LinkageInfoDTO;
import io.dataease.api.visualization.dto.VisualizationLinkageDTO;
import io.dataease.api.visualization.vo.VisualizationLinkageFieldVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.dao.auto.entity.QCoreDatasetTableField;
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
    private CoreDatasetTableFieldRepository coreDatasetTableFieldRepository;



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

        // 1. 联动列表只需基础信息，避免加载完整的图表配置。
        QSnapshotCoreChartView chartView = QSnapshotCoreChartView.snapshotCoreChartView;
        List<VisualizationViewTableVO> targetViews = queryFactory
                .select(Projections.fields(VisualizationViewTableVO.class,
                        chartView.id,
                        chartView.type,
                        chartView.tableId,
                        chartView.title))
                .from(chartView)
                .where(chartView.id.in(targetViewIdList), chartView.type.ne("VQuery"))
                .fetch();

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
                List<DatasetTableFieldDTO> fields = coreDatasetTableFieldRepository.findByDatasetGroupId(targetView.getTableId())
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

        // 1. 联动列表只需基础信息，避免加载完整的图表配置。
        QCoreChartView chartView = QCoreChartView.coreChartView;
        List<VisualizationViewTableVO> targetViews = queryFactory
                .select(Projections.fields(VisualizationViewTableVO.class,
                        chartView.id,
                        chartView.type,
                        chartView.tableId,
                        chartView.title))
                .from(chartView)
                .where(chartView.id.in(targetViewIdList), chartView.type.ne("VQuery"))
                .fetch();

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
                List<DatasetTableFieldDTO> fields = Optional.ofNullable(
                                coreDatasetTableFieldRepository.findByDatasetTableId(targetView.getTableId())
                        )
                        .orElse(Collections.emptyList())
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
        QVisualizationLinkage linkage = QVisualizationLinkage.visualizationLinkage;
        QVisualizationLinkageField linkageField = QVisualizationLinkageField.visualizationLinkageField;
        QCoreChartView sourceView = new QCoreChartView("sourceView");
        QCoreChartView targetView = new QCoreChartView("targetView");
        QCoreDatasetTableField sourceField = new QCoreDatasetTableField("sourceField");
        QCoreDatasetTableField targetField = new QCoreDatasetTableField("targetField");
        return collectLinkageInfo(queryFactory
                .select(sourceView.id, sourceField.id, targetView.id, targetField.id)
                .from(linkage, linkageField, sourceView, targetView, sourceField, targetField)
                .where(linkage.dvId.eq(dvId), linkage.linkageActive.isTrue(), linkageField.linkageId.eq(linkage.id),
                        sourceView.id.eq(linkage.sourceViewId), sourceView.sceneId.eq(dvId), sourceView.linkageActive.isTrue(),
                        targetView.id.eq(linkage.targetViewId), targetView.sceneId.eq(dvId),
                        sourceField.id.eq(linkageField.sourceField), sourceFieldMatchesChart(sourceField, sourceView.id, sourceView.tableId),
                        targetField.id.eq(linkageField.targetField), targetField.datasetGroupId.eq(targetView.tableId))
                .fetch());
    }

    public List<LinkageInfoDTO> getPanelAllLinkageInfoSnapshot(Long dvId) {
        QSnapshotVisualizationLinkage linkage = QSnapshotVisualizationLinkage.snapshotVisualizationLinkage;
        QSnapshotVisualizationLinkageField linkageField = QSnapshotVisualizationLinkageField.snapshotVisualizationLinkageField;
        QSnapshotCoreChartView sourceView = new QSnapshotCoreChartView("sourceView");
        QSnapshotCoreChartView targetView = new QSnapshotCoreChartView("targetView");
        QCoreDatasetTableField sourceField = new QCoreDatasetTableField("sourceField");
        QCoreDatasetTableField targetField = new QCoreDatasetTableField("targetField");
        return collectLinkageInfo(queryFactory
                .select(sourceView.id, sourceField.id, targetView.id, targetField.id)
                .from(linkage, linkageField, sourceView, targetView, sourceField, targetField)
                .where(linkage.dvId.eq(dvId), linkage.linkageActive.isTrue(), linkageField.linkageId.eq(linkage.id),
                        sourceView.id.eq(linkage.sourceViewId), sourceView.sceneId.eq(dvId), sourceView.linkageActive.isTrue(),
                        targetView.id.eq(linkage.targetViewId), targetView.sceneId.eq(dvId),
                        sourceField.id.eq(linkageField.sourceField), sourceFieldMatchesChart(sourceField, sourceView.id, sourceView.tableId),
                        targetField.id.eq(linkageField.targetField), targetField.datasetGroupId.eq(targetView.tableId))
                .fetch());
    }

    private BooleanExpression sourceFieldMatchesChart(QCoreDatasetTableField field, NumberPath<Long> chartId, NumberPath<Long> tableId) {
        BooleanExpression sameDataset = field.datasetGroupId.eq(tableId);
        // 历史图表计算字段可能没有数据集 ID，仍可作为所属图表的联动来源。
        return field.chartId.isNull().and(sameDataset)
                .or(field.chartId.eq(chartId).and(sameDataset.or(field.datasetGroupId.isNull())));
    }

    private List<LinkageInfoDTO> collectLinkageInfo(List<Tuple> fields) {
        Map<String, List<String>> groupedResults = new HashMap<>();
        for (Tuple field : fields) {
            String sourceKey = field.get(0, Long.class) + "#" + field.get(1, Long.class);
            String targetValue = field.get(2, Long.class) + "#" + field.get(3, Long.class);
            groupedResults.computeIfAbsent(sourceKey, key -> new ArrayList<>()).add(targetValue);
        }
        return groupedResults.entrySet().stream()
                .map(entry -> {
                    LinkageInfoDTO dto = new LinkageInfoDTO();
                    dto.setSourceInfo(entry.getKey());
                    dto.setTargetInfoList(entry.getValue());
                    return dto;
                })
                .toList();
    }
}
