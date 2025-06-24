package io.dataease.visualization.manage;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.visualization.dao.auto.entity.QVisualizationLinkage;
import io.dataease.visualization.dao.auto.entity.QVisualizationLinkageField;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkageField;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkageFieldRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkageRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
}
