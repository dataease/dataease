package io.dataease.visualization.manage;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository;
import io.dataease.visualization.utils.VisualizationUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.function.Supplier;

@Component
class VisualizationResourceStore {

    private static final QCoreChartView CHART = QCoreChartView.coreChartView;
    private static final QSnapshotCoreChartView DRAFT_CHART = QSnapshotCoreChartView.snapshotCoreChartView;
    private static final Relation<CoreChartView, SnapshotCoreChartView> CHARTS = new Relation<>(
            new ResourceTable<>(CHART, CHART.sceneId::eq, CoreChartView::new),
            new ResourceTable<>(DRAFT_CHART, DRAFT_CHART.sceneId::eq, SnapshotCoreChartView::new));

    // 关联表清单按父表到子表排列。按同一份归属规则装载与清理，清理顺序与写入相反。
    private static final List<Relation<?, ?>> RELATIONS = relatedTables();

    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private ChartViewManege chartViewManege;

    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;

    @Resource
    private SnapshotDataVisualizationInfoRepository snapshotDataVisualizationInfoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    Set<Long> publishDraft(Long resourceId) {
        requireResource(resourceId);
        SnapshotDataVisualizationInfo draft = entityManager.find(SnapshotDataVisualizationInfo.class, resourceId);
        if (draft == null || Boolean.TRUE.equals(draft.getDeleteFlag())) {
            throw DEException.of(ErrorCategory.NOT_FOUND, "i18n_resource_not_exists");
        }
        if (StringUtils.isBlank(draft.getComponentData())) {
            throw DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
        }

        Set<Long> activeChartIds = VisualizationUtils.activeChartIds(draft.getComponentData());
        replaceRelations(resourceId, false, activeChartIds);

        // 保留主表行及资源身份，原地替换版本化字段，显式同步可空值。
        DataVisualizationInfo published = requireResource(resourceId);
        published.setCanvasStyleData(draft.getCanvasStyleData());
        published.setComponentData(draft.getComponentData());
        published.setMobileLayout(draft.getMobileLayout());
        published.setSelfWatermarkStatus(draft.getSelfWatermarkStatus());
        published.setCheckVersion(draft.getCheckVersion());
        dataVisualizationInfoRepository.saveAndFlush(published);
        chartViewManege.restoreThreshold(resourceId, CommonConstants.RESOURCE_TABLE.CORE);
        return activeChartIds;
    }

    void restoreDraft(Long resourceId) {
        DataVisualizationInfo published = requireResource(resourceId);
        replaceRelations(resourceId, true, VisualizationUtils.activeChartIds(published.getComponentData()));

        SnapshotDataVisualizationInfo draft = entityManager.find(SnapshotDataVisualizationInfo.class, resourceId);
        if (draft == null) {
            draft = new SnapshotDataVisualizationInfo();
        }
        BeanUtils.copyBean(draft, published);
        snapshotDataVisualizationInfoRepository.saveAndFlush(draft);
        chartViewManege.restoreThreshold(resourceId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
    }

    private DataVisualizationInfo requireResource(Long resourceId) {
        DataVisualizationInfo resource = entityManager.find(DataVisualizationInfo.class, resourceId);
        if (resource == null || Boolean.TRUE.equals(resource.getDeleteFlag())) {
            throw DEException.of(ErrorCategory.NOT_FOUND, "i18n_resource_not_exists");
        }
        return resource;
    }

    private void replaceRelations(long resourceId, boolean toDraft, Set<Long> chartIds) {
        List<Transfer> transfers = new ArrayList<>();
        transfers.add(toDraft
                ? loadTransfer(resourceId, CHARTS.published(), CHARTS.draft(), CHART.id.in(chartIds))
                : loadTransfer(resourceId, CHARTS.draft(), CHARTS.published(), DRAFT_CHART.id.in(chartIds)));
        for (Relation<?, ?> relation : RELATIONS) {
            transfers.add(loadTransfer(resourceId, relation, toDraft));
        }

        entityManager.flush();
        for (Transfer transfer : transfers.reversed()) {
            ResourceTable<?> target = transfer.target();
            queryFactory.delete(target.entity()).where(target.scope().apply(resourceId)).execute();
        }
        String targetTable = toDraft ? CommonConstants.RESOURCE_TABLE.SNAPSHOT : CommonConstants.RESOURCE_TABLE.CORE;
        chartViewManege.removeThreshold(resourceId, targetTable);

        // 批量删除后清理持久化上下文，允许同一事务按原 ID 重建。
        entityManager.flush();
        entityManager.clear();
        for (Transfer transfer : transfers) {
            transfer.rows().forEach(entityManager::persist);
        }
    }

    private Transfer loadTransfer(long resourceId, Relation<?, ?> relation, boolean toDraft) {
        return toDraft
                ? loadTransfer(resourceId, relation.published(), relation.draft(), null)
                : loadTransfer(resourceId, relation.draft(), relation.published(), null);
    }

    private <S, T> Transfer loadTransfer(long resourceId, ResourceTable<S> source, ResourceTable<T> target,
                                         Predicate filter) {
        List<T> rows = queryFactory.selectFrom(source.entity()).where(source.scope().apply(resourceId), filter)
                .fetch().stream()
                .map(row -> BeanUtils.copyBean(target.newRow().get(), row))
                .toList();
        return new Transfer(target, rows);
    }

    private static List<Relation<?, ?>> relatedTables() {
        QVisualizationLinkage linkage = QVisualizationLinkage.visualizationLinkage;
        QSnapshotVisualizationLinkage draftLinkage = QSnapshotVisualizationLinkage.snapshotVisualizationLinkage;
        Relation<VisualizationLinkage, SnapshotVisualizationLinkage> linkages = new Relation<>(
                new ResourceTable<>(linkage, linkage.dvId::eq, VisualizationLinkage::new),
                new ResourceTable<>(draftLinkage, draftLinkage.dvId::eq, SnapshotVisualizationLinkage::new));

        QVisualizationLinkageField linkageField = QVisualizationLinkageField.visualizationLinkageField;
        QSnapshotVisualizationLinkageField draftLinkageField = QSnapshotVisualizationLinkageField.snapshotVisualizationLinkageField;
        Relation<VisualizationLinkageField, SnapshotVisualizationLinkageField> linkageFields = new Relation<>(
                new ResourceTable<>(linkageField, childScope(linkageField.linkageId, linkage.id, linkages.published()),
                        VisualizationLinkageField::new),
                new ResourceTable<>(draftLinkageField, childScope(draftLinkageField.linkageId, draftLinkage.id, linkages.draft()),
                        SnapshotVisualizationLinkageField::new));

        QVisualizationLinkJump jump = QVisualizationLinkJump.visualizationLinkJump;
        QSnapshotVisualizationLinkJump draftJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
        Relation<VisualizationLinkJump, SnapshotVisualizationLinkJump> jumps = new Relation<>(
                new ResourceTable<>(jump, jump.sourceDvId::eq, VisualizationLinkJump::new),
                new ResourceTable<>(draftJump, draftJump.sourceDvId::eq, SnapshotVisualizationLinkJump::new));

        QVisualizationLinkJumpInfo jumpInfo = QVisualizationLinkJumpInfo.visualizationLinkJumpInfo;
        QSnapshotVisualizationLinkJumpInfo draftJumpInfo = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
        Relation<VisualizationLinkJumpInfo, SnapshotVisualizationLinkJumpInfo> jumpInfos = new Relation<>(
                new ResourceTable<>(jumpInfo, childScope(jumpInfo.linkJumpId, jump.id, jumps.published()), VisualizationLinkJumpInfo::new),
                new ResourceTable<>(draftJumpInfo, childScope(draftJumpInfo.linkJumpId, draftJump.id, jumps.draft()),
                        SnapshotVisualizationLinkJumpInfo::new));

        QVisualizationLinkJumpTargetViewInfo jumpTarget = QVisualizationLinkJumpTargetViewInfo.visualizationLinkJumpTargetViewInfo;
        QSnapshotVisualizationLinkJumpTargetViewInfo draftJumpTarget = QSnapshotVisualizationLinkJumpTargetViewInfo.snapshotVisualizationLinkJumpTargetViewInfo;
        Relation<VisualizationLinkJumpTargetViewInfo, SnapshotVisualizationLinkJumpTargetViewInfo> jumpTargets = new Relation<>(
                new ResourceTable<>(jumpTarget, childScope(jumpTarget.linkJumpInfoId, jumpInfo.id, jumpInfos.published()),
                        VisualizationLinkJumpTargetViewInfo::new),
                new ResourceTable<>(draftJumpTarget, childScope(draftJumpTarget.linkJumpInfoId, draftJumpInfo.id, jumpInfos.draft()),
                        SnapshotVisualizationLinkJumpTargetViewInfo::new));

        QVisualizationOuterParams param = QVisualizationOuterParams.visualizationOuterParams;
        QSnapshotVisualizationOuterParams draftParam = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
        Relation<VisualizationOuterParams, SnapshotVisualizationOuterParams> outerParams = new Relation<>(
                new ResourceTable<>(param, id -> param.visualizationId.eq(Long.toString(id)), VisualizationOuterParams::new),
                new ResourceTable<>(draftParam, id -> draftParam.visualizationId.eq(Long.toString(id)), SnapshotVisualizationOuterParams::new));

        QVisualizationOuterParamsInfo paramInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
        QSnapshotVisualizationOuterParamsInfo draftParamInfo = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;
        Relation<VisualizationOuterParamsInfo, SnapshotVisualizationOuterParamsInfo> outerParamInfos = new Relation<>(
                new ResourceTable<>(paramInfo, childScope(paramInfo.paramsId, param.paramsId, outerParams.published()),
                        VisualizationOuterParamsInfo::new),
                new ResourceTable<>(draftParamInfo, childScope(draftParamInfo.paramsId, draftParam.paramsId, outerParams.draft()),
                        SnapshotVisualizationOuterParamsInfo::new));

        QVisualizationOuterParamsTargetViewInfo paramTarget = QVisualizationOuterParamsTargetViewInfo.visualizationOuterParamsTargetViewInfo;
        QSnapshotVisualizationOuterParamsTargetViewInfo draftParamTarget = QSnapshotVisualizationOuterParamsTargetViewInfo.snapshotVisualizationOuterParamsTargetViewInfo;
        Relation<VisualizationOuterParamsTargetViewInfo, SnapshotVisualizationOuterParamsTargetViewInfo> outerParamTargets = new Relation<>(
                new ResourceTable<>(paramTarget, childScope(paramTarget.paramsInfoId, paramInfo.paramsInfoId, outerParamInfos.published()),
                        VisualizationOuterParamsTargetViewInfo::new),
                new ResourceTable<>(draftParamTarget, childScope(draftParamTarget.paramsInfoId, draftParamInfo.paramsInfoId, outerParamInfos.draft()),
                        SnapshotVisualizationOuterParamsTargetViewInfo::new));

        return List.of(linkages, linkageFields, jumps, jumpInfos, jumpTargets, outerParams, outerParamInfos, outerParamTargets);
    }

    private static <K> LongFunction<Predicate> childScope(SimpleExpression<K> foreignKey, SimpleExpression<K> parentId,
                                                         ResourceTable<?> parent) {
        return resourceId -> foreignKey.in(JPAExpressions.select(parentId).from(parent.entity()).where(parent.scope().apply(resourceId)));
    }

    private record ResourceTable<T>(EntityPath<T> entity, LongFunction<Predicate> scope, Supplier<T> newRow) {
    }

    private record Relation<P, D>(ResourceTable<P> published, ResourceTable<D> draft) {
    }

    private record Transfer(ResourceTable<?> target, List<?> rows) {
    }
}
