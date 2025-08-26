package io.dataease.visualization.manage;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoExtendDTO;
import io.dataease.api.visualization.vo.VisualizationLinkJumpTargetViewInfoVO;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.dao.auto.entity.QCoreDatasetTableField;
import io.dataease.dao.auto.entity.QDataVisualizationInfo;
import io.dataease.share.dao.auto.entity.QXpackShare;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpInfoRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpTargetViewInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class VisualizationLinkJumpManage {

    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private VisualizationLinkJumpRepository visualizationLinkJumpRepository;

    @Resource
    private VisualizationLinkJumpInfoRepository visualizationLinkJumpInfoRepository;

    @Resource
    private VisualizationLinkJumpTargetViewInfoRepository visualizationLinkJumpTargetViewInfoRepository;
    private final QXpackShare qXpackShare = QXpackShare.xpackShare;

    public void copyLinkJump(Long copyId) {
        // 1. 查询需要复制的源数据
        QCoreChartView pvs = QCoreChartView.coreChartView;
        QCoreChartView pvt = QCoreChartView.coreChartView;

        // 获取视图映射关系
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

        // 2. 构建映射关系Map (使用Map.Entry替代Pair)
        Map<Map.Entry<Long, Long>, Map.Entry<Long, Long>> mappingMap = viewMappings.stream()
                .collect(Collectors.toMap(
                        t -> Map.entry(t.get(0, Long.class), t.get(1, Long.class)),
                        t -> Map.entry(t.get(2, Long.class), t.get(3, Long.class)),
                        (existing, replacement) -> existing // 如果有重复键，保留已存在的
                ));

        // 3. 查询符合条件的源linkJump记录
        List<Long> sourceDvIds = viewMappings.stream()
                .map(t -> t.get(0, Long.class))
                .distinct()
                .collect(Collectors.toList());

        List<VisualizationLinkJump> sourceLinks = queryFactory
                .selectFrom(QVisualizationLinkJump.visualizationLinkJump)
                .where(QVisualizationLinkJump.visualizationLinkJump.sourceDvId.in(sourceDvIds))
                .fetch();

        // 4. 创建新记录并批量保存
        List<VisualizationLinkJump> newLinks = sourceLinks.stream()
                .map(source -> {
                    Map.Entry<Long, Long> sourceKey = Map.entry(source.getSourceDvId(), source.getSourceViewId());
                    Map.Entry<Long, Long> target = mappingMap.get(sourceKey);

                    if (target != null) {
                        VisualizationLinkJump newLink = new VisualizationLinkJump();
                        newLink.setId(source.getId() + copyId);
                        newLink.setSourceDvId(target.getKey());  // getKey() 对应原来的 getLeft()
                        newLink.setSourceViewId(target.getValue()); // getValue() 对应原来的 getRight()
                        newLink.setLinkJumpInfo(source.getLinkJumpInfo());
                        newLink.setChecked(source.getChecked());
                        newLink.setCopyFrom(source.getId());
                        newLink.setCopyId(copyId);
                        return newLink;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. 批量保存
        visualizationLinkJumpRepository.saveAll(newLinks);
    }


    public void copyLinkJumpInfo(Long copyId) {
        // 1. 查询需要复制的映射关系
        QVisualizationLinkJump linkJump = QVisualizationLinkJump.visualizationLinkJump;

        // 获取linkJump的映射关系 (原ID -> 新ID)
        List<Tuple> jumpMappings = queryFactory
                .select(
                        linkJump.id.as("originalId"),
                        linkJump.copyFrom.as("sourceId"))
                .from(linkJump)
                .where(linkJump.copyId.eq(copyId))
                .fetch();

        if (jumpMappings.isEmpty()) {
            return;
        }

        // 2. 构建映射关系Map (使用Map.Entry替代Pair)
        Map<Long, Long> jumpIdMap = jumpMappings.stream()
                .collect(Collectors.toMap(
                        t -> t.get(1, Long.class),  // sourceId (原ID)
                        t -> t.get(0, Long.class),  // originalId (新ID)
                        (existing, replacement) -> existing
                ));

        // 3. 查询需要复制的源linkJumpInfo记录
        List<VisualizationLinkJumpInfo> sourceInfos = queryFactory
                .selectFrom(QVisualizationLinkJumpInfo.visualizationLinkJumpInfo)
                .where(QVisualizationLinkJumpInfo.visualizationLinkJumpInfo.linkJumpId.in(jumpIdMap.keySet()))
                .fetch();

        // 4. 创建新记录并批量保存
        List<VisualizationLinkJumpInfo> newInfos = sourceInfos.stream()
                .map(source -> {
                    Long newLinkJumpId = jumpIdMap.get(source.getLinkJumpId());
                    if (newLinkJumpId != null) {
                        VisualizationLinkJumpInfo newInfo = new VisualizationLinkJumpInfo();
                        newInfo.setId(source.getId() + copyId);
                        newInfo.setLinkJumpId(newLinkJumpId);
                        newInfo.setLinkType(source.getLinkType());
                        newInfo.setJumpType(source.getJumpType());
                        newInfo.setTargetDvId(source.getTargetDvId());
                        newInfo.setSourceFieldId(source.getSourceFieldId());
                        newInfo.setContent(source.getContent());
                        newInfo.setChecked(source.getChecked());
                        newInfo.setAttachParams(source.getAttachParams());
                        newInfo.setCopyFrom(source.getId());
                        newInfo.setCopyId(copyId);
                        newInfo.setWindowSize(source.getWindowSize());
                        return newInfo;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. 批量保存
        visualizationLinkJumpInfoRepository.saveAll(newInfos);
    }


    public void copyLinkJumpTargetInfo(Long copyId) {
        // 1. 查询需要复制的映射关系（link_jump_info_id 映射）
        QVisualizationLinkJumpInfo linkJumpInfo = QVisualizationLinkJumpInfo.visualizationLinkJumpInfo;

        List<Tuple> infoMappings = queryFactory
                .select(
                        linkJumpInfo.id.as("newId"),
                        linkJumpInfo.copyFrom.as("originalId"))
                .from(linkJumpInfo)
                .where(linkJumpInfo.copyId.eq(copyId))
                .fetch();

        if (infoMappings.isEmpty()) {
            return;
        }

        // 2. 构建ID映射Map
        Map<Long, Long> infoIdMap = infoMappings.stream()
                .collect(Collectors.toMap(
                        t -> t.get(1, Long.class),  // originalId
                        t -> t.get(0, Long.class),  // newId
                        (existing, replacement) -> existing
                ));

        // 3. 查询需要复制的源target记录
        List<VisualizationLinkJumpTargetViewInfo> sourceTargets = queryFactory
                .selectFrom(QVisualizationLinkJumpTargetViewInfo.visualizationLinkJumpTargetViewInfo)
                .where(QVisualizationLinkJumpTargetViewInfo.visualizationLinkJumpTargetViewInfo
                        .linkJumpInfoId.in(infoIdMap.keySet()))
                .fetch();

        // 4. 创建新记录并批量保存
        List<VisualizationLinkJumpTargetViewInfo> newTargets = sourceTargets.stream()
                .map(source -> {
                    Long newLinkJumpInfoId = infoIdMap.get(source.getLinkJumpInfoId());
                    if (newLinkJumpInfoId != null) {
                        VisualizationLinkJumpTargetViewInfo newTarget = new VisualizationLinkJumpTargetViewInfo();
                        newTarget.setTargetId(source.getTargetId() + copyId);
                        newTarget.setLinkJumpInfoId(newLinkJumpInfoId);
                        newTarget.setSourceFieldActiveId(source.getSourceFieldActiveId());
                        newTarget.setTargetViewId(source.getTargetViewId());
                        newTarget.setTargetFieldId(source.getTargetFieldId());
                        newTarget.setCopyFrom(source.getTargetId());
                        newTarget.setCopyId(copyId);
                        newTarget.setTargetType(source.getTargetType());
                        return newTarget;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. 批量保存
        visualizationLinkJumpTargetViewInfoRepository.saveAll(newTargets);
    }


    public List<VisualizationLinkJumpDTO> queryWithDvId(Long dvId, Long uid, Boolean isDesktop) {
        return buildLinkJumpQuery(dvId, uid, isDesktop, false).fetch();
    }

    public List<VisualizationLinkJumpDTO> queryWithDvIdSnapshot(Long dvId, Long uid, Boolean isDesktop) {
        return buildLinkJumpQuery(dvId, uid, isDesktop, true).fetch();
    }

    public VisualizationLinkJumpDTO queryWithViewId(Long dvId, Long viewId, Long uid, Boolean isDesktop) {
        QSnapshotCoreChartView qChartView = QSnapshotCoreChartView.snapshotCoreChartView;
        QSnapshotVisualizationLinkJump qJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
        VisualizationLinkJumpDTO result = queryFactory
                .select(Projections.bean(VisualizationLinkJumpDTO.class,
                        qChartView.id.as("sourceViewId"),
                        qJump.id,
                        Expressions.asNumber(dvId).as("sourceDvId"),
                        qJump.linkJumpInfo,
                        Expressions.asBoolean(qJump.checked).as("checked")
                ))
                .from(qChartView)
                .leftJoin(qJump).on(qChartView.id.eq(qJump.sourceViewId).and(qJump.sourceDvId.eq(dvId)))
                .where(qChartView.id.eq(viewId)).fetchFirst();
        if (result != null) {
            result.setLinkJumpInfoArray(getLinkJumpInfoSnapshot(result.getId() == null?-1:result.getId(), result.getSourceViewId(), uid, isDesktop));
        }
        return result;
    }

    private List<VisualizationLinkJumpInfoExtendDTO> queryBaseLinkJumpInfoSnapshot(Long id, Long sourceViewId, Long uid, boolean isDesktop) {
        QSnapshotCoreChartView ccv = QSnapshotCoreChartView.snapshotCoreChartView;
        QCoreDatasetTableField cdtf = QCoreDatasetTableField.coreDatasetTableField;
        QSnapshotVisualizationLinkJump vlj = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
        QSnapshotVisualizationLinkJumpInfo vlji = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
        QDataVisualizationInfo dvi = QDataVisualizationInfo.dataVisualizationInfo;
        QSnapshotVisualizationLinkJumpTargetViewInfo vljtvi = QSnapshotVisualizationLinkJumpTargetViewInfo.snapshotVisualizationLinkJumpTargetViewInfo;
        QXpackShare xpackShare = QXpackShare.xpackShare;
        QSnapshotVisualizationOuterParamsInfo vopi = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;

        // 构建查询，直接返回VisualizationLinkJumpInfoDTO
        JPAQuery<VisualizationLinkJumpInfoExtendDTO> query = queryFactory
                .select(Projections.bean(VisualizationLinkJumpInfoExtendDTO.class,
                        cdtf.id.as("sourceFieldId"),
                        cdtf.deType.as("sourceDeType"),
                        cdtf.name.as("sourceFieldName"),
                        vlji.id,
                        vlji.linkJumpId,
                        vlji.linkType,
                        vlji.jumpType,
                        vlji.windowSize,
                        vlji.targetDvId,
                        dvi.type.as("targetDvType"),
                        vlji.content,
                        Expressions.cases()
                                .when(vlji.checked.isNull()).then(false)
                                .otherwise(vlji.checked).as("checked"),
                        Expressions.cases()
                                .when(vlji.attachParams.isNull()).then(false)
                                .otherwise(vlji.attachParams).as("attachParams"),
                        vljtvi.targetId,
                        vljtvi.targetViewId,
                        vljtvi.targetFieldId,
                        vljtvi.targetType,
                        vljtvi.sourceFieldActiveId,
                        vopi.paramName.as("outerParamsName")
                ))
                .from(ccv)
                .leftJoin(cdtf).on(ccv.tableId.eq(cdtf.datasetGroupId))
                .leftJoin(vlj).on(ccv.id.eq(vlj.sourceViewId).and(vlj.id.eq(id)))
                .leftJoin(vlji).on(vlj.id.eq(vlji.linkJumpId).and(cdtf.id.eq(vlji.sourceFieldId)))
                .leftJoin(dvi).on(vlji.targetDvId.eq(dvi.id))
                .leftJoin(vljtvi).on(vlji.id.eq(vljtvi.linkJumpInfoId))
                .leftJoin(vopi).on(vopi.paramsInfoId.eq(vljtvi.targetViewId));

        // 动态添加 xpack_share 连接和字段（非桌面版）
        if (!isDesktop) {
            query.leftJoin(xpackShare).on(
                    xpackShare.creator.eq(uid)
                            .and(vlji.targetDvId.eq(xpackShare.resourceId))
            );
            // 重新构建select包含publicJumpId
            query.select(Projections.bean(VisualizationLinkJumpInfoExtendDTO.class,
                    cdtf.id.as("sourceFieldId"),
                    cdtf.deType.as("sourceDeType"),
                    cdtf.name.as("sourceFieldName"),
                    vlji.id,
                    vlji.linkJumpId,
                    vlji.linkType,
                    vlji.jumpType,
                    vlji.windowSize,
                    vlji.targetDvId,
                    dvi.type.as("targetDvType"),
                    vlji.content,
                    Expressions.cases()
                            .when(vlji.checked.isNull()).then(false)
                            .otherwise(vlji.checked).as("checked"),
                    Expressions.cases()
                            .when(vlji.attachParams.isNull()).then(false)
                            .otherwise(vlji.attachParams).as("attachParams"),
                    vljtvi.targetId,
                    vljtvi.targetViewId,
                    vljtvi.targetFieldId,
                    vljtvi.targetType,
                    vljtvi.sourceFieldActiveId,
                    vopi.paramName.as("outerParamsName"),
                    xpackShare.uuid.as("publicJumpId")
            ));
        }
        query.where(ccv.id.eq(sourceViewId).and(ccv.type.ne("VQuery")));
        query.orderBy(cdtf.name.asc());
        return query.fetch();
    }

    private List<VisualizationLinkJumpInfoDTO> aggregateTargetViewInfo(List<VisualizationLinkJumpInfoExtendDTO> baseResults) {
        // 使用分组键：targetDvType + sourceFieldId + sourceDeType + sourceFieldName + publicJumpId
        Map<String, VisualizationLinkJumpInfoDTO> groupMap = new LinkedHashMap<>();

        for (VisualizationLinkJumpInfoExtendDTO dto : baseResults) {
            // 构建分组键
            String groupKey = buildGroupKey(dto);
            // 获取或创建聚合后的DTO
            VisualizationLinkJumpInfoDTO aggregatedDto = groupMap.computeIfAbsent(groupKey, k -> {
                VisualizationLinkJumpInfoDTO newDto = new VisualizationLinkJumpInfoDTO();
                // 复制基本字段
                newDto.setId(dto.getId());
                newDto.setLinkJumpId(dto.getLinkJumpId());
                newDto.setLinkType(dto.getLinkType());
                newDto.setJumpType(dto.getJumpType());
                newDto.setWindowSize(dto.getWindowSize());
                newDto.setTargetDvId(dto.getTargetDvId());
                newDto.setTargetDvType(dto.getTargetDvType());
                newDto.setContent(dto.getContent());
                newDto.setChecked(dto.getChecked());
                newDto.setAttachParams(dto.getAttachParams());
                newDto.setSourceFieldId(dto.getSourceFieldId());
                newDto.setSourceDeType(dto.getSourceDeType());
                newDto.setSourceFieldName(dto.getSourceFieldName());
                newDto.setPublicJumpId(dto.getPublicJumpId());
                newDto.setTargetViewInfoList(new ArrayList<>());
                return newDto;
            });

            // 添加目标视图信息到集合中（如果存在目标视图信息）
            if (dto.getTargetId() != null || dto.getTargetViewId() != null) {
                VisualizationLinkJumpTargetViewInfoVO targetViewInfo = new VisualizationLinkJumpTargetViewInfoVO();
                targetViewInfo.setTargetId(dto.getTargetId());
                targetViewInfo.setTargetViewId(dto.getTargetViewId());
                targetViewInfo.setTargetFieldId(dto.getTargetFieldId());
                targetViewInfo.setTargetType(dto.getTargetType());
                targetViewInfo.setSourceFieldActiveId(dto.getSourceFieldActiveId());
                targetViewInfo.setOuterParamsName(dto.getOuterParamsName());

                aggregatedDto.getTargetViewInfoList().add(targetViewInfo);
            }
        }

        // 返回分组聚合后的DTO列表
        return new ArrayList<>(groupMap.values());
    }

    private String buildGroupKey(VisualizationLinkJumpInfoExtendDTO dto) {
        return String.format("%s_%s_%s_%s_%s",
                dto.getTargetDvType() != null ? dto.getTargetDvType() : "",
                dto.getSourceFieldId() != null ? dto.getSourceFieldId() : "",
                dto.getSourceDeType() != null ? dto.getSourceDeType() : "",
                dto.getSourceFieldName() != null ? dto.getSourceFieldName() : "",
                dto.getPublicJumpId() != null ? dto.getPublicJumpId() : ""
        );
    }

    private JPAQuery<VisualizationLinkJumpDTO> buildLinkJumpQuery(Long dvId, Long uid, Boolean isDesktop, boolean isSnapshot) {

        if (isSnapshot) {
            QSnapshotCoreChartView qChartView = QSnapshotCoreChartView.snapshotCoreChartView;
            QSnapshotVisualizationLinkJump qJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
            return queryFactory
                    .select(Projections.bean(VisualizationLinkJumpDTO.class,
                            qChartView.id.as("sourceViewId"),
                            qJump.id,
                            Expressions.asNumber(dvId).as("sourceDvId"),
                            qJump.linkJumpInfo,
                            qJump.checked
                    ))
                    .from(qChartView)
                    .leftJoin(qJump).on(qChartView.id.eq(qJump.sourceViewId))
                    .where(qJump.sourceDvId.eq(dvId))
                    .where(qChartView.jumpActive.eq(true));
        } else {
            QCoreChartView qChartView = QCoreChartView.coreChartView;
            QVisualizationLinkJump qJump = QVisualizationLinkJump.visualizationLinkJump;

            return queryFactory
                    .select(Projections.bean(VisualizationLinkJumpDTO.class,
                            qChartView.id.as("sourceViewId"),
                            qJump.id,
                            Expressions.asNumber(dvId).as("sourceDvId"),
                            qJump.linkJumpInfo,
                            qJump.checked
                    ))
                    .from(qChartView)
                    .leftJoin(qJump).on(qChartView.id.eq(qJump.sourceViewId))
                    .where(qJump.sourceDvId.eq(dvId))
                    .where(qChartView.jumpActive.eq(true));
        }


    }

    public List<VisualizationLinkJumpInfoDTO> getLinkJumpInfo(String jumpId, Long sourceViewId, Long uid, Boolean isDesktop) {
        return buildLinkJumpInfoQuery(jumpId, sourceViewId, uid, isDesktop, false).fetch();
    }

    public List<VisualizationLinkJumpInfoDTO> getLinkJumpInfoSnapshot(Long id, Long sourceViewId, Long uid, boolean isDesktop) {
        // 查询所有基础数据
        List<VisualizationLinkJumpInfoExtendDTO> baseResults = queryBaseLinkJumpInfoSnapshot(id, sourceViewId, uid, isDesktop);
        // 按照指定字段分组并聚合targetViewInfoList
        return aggregateTargetViewInfo(baseResults);
    }

    private JPAQuery<VisualizationLinkJumpInfoDTO> buildLinkJumpInfoQuery(String jumpId, Long sourceViewId, Long uid, Boolean isDesktop, boolean isSnapshot) {


        return null;
    }

    public void deleteJumpTargetViewInfoSnapshot(Long dvId, Long viewId) {

    }

    public void deleteJumpInfoSnapshot(Long dvId, Long viewId) {

    }

    public void deleteJumpSnapshot(Long dvId, Long viewId) {

    }
}
