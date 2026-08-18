package io.dataease.visualization.manage;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.visualization.dto.VisualizationViewTableDTO;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationReportFilterVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.constants.OptConstants;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.*;
import io.dataease.dao.auto.entity.QCoreDatasetGroup;
import io.dataease.dao.auto.entity.QCoreDatasource;
import io.dataease.dao.auto.entity.QDataVisualizationInfo;
import io.dataease.dao.auto.entity.QPerBusiResource;
import io.dataease.operation.dao.auto.entity.QCoreOptRecent;
import io.dataease.dao.auto.repo.VisualizationReportFilterRepository;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.license.config.XpackInteract;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.PageResult;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.entity.QCoreStore;
import io.dataease.visualization.dao.auto.mapper.*;
import io.dataease.visualization.dao.ext.po.VisualizationNodePO;
import io.dataease.visualization.dao.ext.po.VisualizationResourcePO;
import io.dataease.visualization.dto.VisualizationNodeBO;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class CoreVisualizationManage {
    @Resource
    private JPAQueryFactory queryFactory;
    @PersistenceContext
    private EntityManager entityManager;
    @Resource
    CoreChartViewRepository coreChartViewRepository;
    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;

    @Resource
    private SnapshotDataVisualizationInfoRepository snapshotDataVisualizationInfoRepository;

    @Resource
    private VisualizationOuterParamsTargetViewInfoRepository visualizationOuterParamsTargetViewInfoRepository;
    @Resource
    private VisualizationLinkageFieldRepository visualizationLinkageFieldRepository;
    @Resource
    private VisualizationLinkageRepository visualizationLinkageRepository;
    @Resource
    private VisualizationLinkJumpRepository visualizationLinkJumpRepository;
    @Resource
    private VisualizationLinkJumpInfoRepository visualizationLinkJumpInfoRepository;
    @Resource
    private VisualizationLinkJumpTargetViewInfoRepository visualizationLinkJumpTargetViewInfoRepository;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private ChartViewManege chartViewManege;
    @Autowired
    private SnapshotCoreChartViewRepository snapshotCoreChartViewRepository;

    @Resource
    private VisualizationOuterParamsRepository visualizationOuterParamsRepository;
    @Resource
    private SnapshotVisualizationOuterParamsRepository snapshotVisualizationOuterParamsRepository;
    @Resource
    private SnapshotVisualizationOuterParamsInfoRepository snapshotVisualizationOuterParamsInfoRepository;
    @Resource
    private SnapshotVisualizationOuterParamsTargetViewInfoRepository snapshotVisualizationOuterParamsTargetViewInfoRepository;
    @Resource
    private SnapshotVisualizationLinkageRepository snapshotVisualizationLinkageRepository;
    @Resource
    private SnapshotVisualizationLinkageFieldRepository snapshotVisualizationLinkageFieldRepository;
    @Resource
    private SnapshotVisualizationLinkJumpRepository snapshotVisualizationLinkJumpRepository;
    @Autowired
    private VisualizationOuterParamsInfoRepository visualizationOuterParamsInfoRepository;
    @Resource
    private SnapshotVisualizationLinkJumpInfoRepository snapshotVisualizationLinkJumpInfoRepository;
    @Resource
    private SnapshotVisualizationLinkJumpTargetViewInfoRepository snapshotVisualizationLinkJumpTargetViewInfoRepository;
    @Resource
    private VisualizationReportFilterRepository reportFilterRepository;

    @XpackInteract(value = "visualizationResourceTree", replace = true, invalid = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        List<VisualizationNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) {
            nodes.add(rootNode());
        }
        QDataVisualizationInfo dataVisualizationInfo = QDataVisualizationInfo.dataVisualizationInfo;
        JPAQuery<VisualizationNodePO> query = queryFactory.select(
                        Projections.fields(VisualizationNodePO.class,
                                dataVisualizationInfo.id.castToNum(Long.class).as("id"),
                                dataVisualizationInfo.name,
                                dataVisualizationInfo.pid.castToNum(Long.class).as("pid"),
                                dataVisualizationInfo.nodeType,
                                dataVisualizationInfo.mobileLayout.castToNum(Integer.class).as("extraFlag"),
                                dataVisualizationInfo.status.as("extraFlag1")))
                .from(dataVisualizationInfo)
                .where(dataVisualizationInfo.deleteFlag.eq(false).or(dataVisualizationInfo.deleteFlag.isNull()))
                .where(dataVisualizationInfo.pid.ne(-1L))
                .where(dataVisualizationInfo.type.eq(request.getBusiFlag()))
                .orderBy(dataVisualizationInfo.createTime.desc());
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable())) {
            query.where(dataVisualizationInfo.status.in(Arrays.asList(1, 2)));
        }
        if (ObjectUtils.isNotEmpty(request.getLeaf())) {
            query.where(dataVisualizationInfo.nodeType.eq(request.getLeaf() ? "leaf" : "folder"));
        }
        if (CommunityUtils.isCommunityMode()) {
            int rtId = StringUtils.equalsAny(request.getBusiFlag(), "dataV", "screen") ? BusiResourceEnum.SCREEN.getFlag() : BusiResourceEnum.PANEL.getFlag();
            query.where(CommunityUtils.buildNotExistsCondition(dataVisualizationInfo, rtId));
        }

        List<VisualizationNodePO> pos = query.fetch();
        if (CollectionUtils.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(this::convert).toList());
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    public void dataVisualizationInit() {
        List<Long> resourceIds = dataVisualizationInfoRepository.findByPid(-1L)
                .stream().map(DataVisualizationInfo::getId).toList();
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            resourceIds.forEach(this::delete);
        }
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void delete(Long id) {
        DataVisualizationInfo info = dataVisualizationInfoRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(info)) {
            DEException.throwException("resource not exist");
        }
        Set<Long> delIds = new LinkedHashSet<>();
        Stack<Long> stack = new Stack<>();
        stack.add(id);
        while (!stack.isEmpty()) {
            Long tempPid = stack.pop();
            if (isTopNode(tempPid)) continue;
            delIds.add(tempPid);


            List<Long> childrenIdList = dataVisualizationInfoRepository.queryChildrenId(tempPid);
            if (CollectionUtils.isNotEmpty(childrenIdList)) {
                childrenIdList.forEach(kid -> {
                    if (!delIds.contains(kid)) {
                        stack.add(kid);
                    }
                });
            }
        }
        // 删除可视化资源
        snapshotDataVisualizationInfoRepository.deleteAllByIdInBatch(delIds);
        dataVisualizationInfoRepository.deleteAllByIdInBatch(delIds);

        // 删除图表信息
        coreChartViewRepository.deleteBySceneIds(delIds);
        snapshotCoreChartViewRepository.deleteBySceneIds(delIds);


        coreOptRecentManage.saveOpt(id, OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.DELETE);
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void move(DataVisualizationBaseRequest request) {
        if (!request.getMoveFromUpdate()) {
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            if (ObjectUtils.isEmpty(visualizationInfo.getId())) {
                DEException.throwException("resource not exist");
            }
            visualizationInfo.setUpdateTime(System.currentTimeMillis());
            SnapshotDataVisualizationInfo snapshotVisualizationInfo = new SnapshotDataVisualizationInfo();
            BeanUtils.copyBean(snapshotVisualizationInfo, visualizationInfo);
            coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.UPDATE);
            dataVisualizationInfoRepository.saveAndFlush(visualizationInfo);
            snapshotDataVisualizationInfoRepository.saveAndFlush(snapshotVisualizationInfo);
        }
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public Long innerSave(DataVisualizationInfo visualizationInfo) {
        visualizationInfo.setVersion(3);
        return preInnerSave(visualizationInfo);
    }

    public Long preInnerSave(DataVisualizationInfo visualizationInfo) {
        if (visualizationInfo.getId() == null) {
            Long id = IDUtils.snowID();
            visualizationInfo.setId(id);
        }

        Long uid = V3UserUtil.getUid();
        java.lang.Long oid = V3UserUtil.getUser().getOid();
        visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
        visualizationInfo.setStatus(visualizationInfo.getStatus());
        visualizationInfo.setCreateBy(uid.toString());
        visualizationInfo.setUpdateBy(uid.toString());
        visualizationInfo.setCreateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setOrgId(oid);
        dataVisualizationInfoRepository.saveAndFlush(visualizationInfo);
        // 镜像文件插入
        SnapshotDataVisualizationInfo snapshotVisualizationInfo = new SnapshotDataVisualizationInfo();
        BeanUtils.copyBean(snapshotVisualizationInfo, visualizationInfo);
        snapshotDataVisualizationInfoRepository.saveAndFlush(snapshotVisualizationInfo);
        coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.NEW);
        return Long.valueOf(visualizationInfo.getId());
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void innerEdit(DataVisualizationInfo visualizationInfo) {
        if (Boolean.TRUE.equals(visualizationInfo.getOrgRoot())) {
            return;
        }
        // 镜像和主表保持名称一致
        Long uid = V3UserUtil.getUid();
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateBy(uid.toString());
        visualizationInfo.setVersion(3);
        // 更新镜像
        SnapshotDataVisualizationInfo snapshotVisualizationInfo = new SnapshotDataVisualizationInfo();
        BeanUtils.copyBean(snapshotVisualizationInfo, visualizationInfo);
        snapshotDataVisualizationInfoRepository.saveAndFlush(snapshotVisualizationInfo);
        // 更新主表名称
        DataVisualizationInfo coreVisualizationInfo = new DataVisualizationInfo();
        coreVisualizationInfo.setId(visualizationInfo.getId());
        coreVisualizationInfo.setStatus(visualizationInfo.getStatus());
        coreVisualizationInfo.setPid(visualizationInfo.getPid());
        coreVisualizationInfo.setContentId(visualizationInfo.getContentId());
        coreVisualizationInfo.setName(visualizationInfo.getName());
        coreVisualizationInfo.setUpdateTime(System.currentTimeMillis());
        coreVisualizationInfo.setUpdateBy(uid.toString());
        coreVisualizationInfo.setVersion(3);
        dataVisualizationInfoRepository.saveAndFlush(coreVisualizationInfo);
        coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.UPDATE);
    }

    private boolean isTopNode(Long pid) {
        return ObjectUtils.isEmpty(pid) || pid.equals(0L);
    }

    private VisualizationNodeBO rootNode() {
        return new VisualizationNodeBO(0L, "root", false, 7, -1L, 0, 1);
    }

    private VisualizationNodeBO convert(VisualizationNodePO po) {
        return new VisualizationNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), "leaf"), 9, po.getPid(), po.getExtraFlag(), po.getExtraFlag1());
    }

    public CoreVisualizationManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    @XpackInteract(value = "perFilterManage", recursion = true, invalid = true)
    public PageResult<VisualizationResourceVO> query(int pageNum, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Page<VisualizationResourcePO> poPage = proxy().queryVisualizationPage(pageNum, pageSize, request);
        if (poPage == null || poPage.getContent().isEmpty()) {
            return new PageResult<>();
        }
        Page<VisualizationResourceVO> visualizationResourcePOPageIPage = poPage.map(po -> {
            return new VisualizationResourceVO(
                    po.getId(), po.getResourceId(), po.getName(),
                    po.getType(), String.valueOf(po.getCreator()), String.valueOf(po.getLastEditor()), po.getLastEditTime(),
                    po.getFavorite(), 9, po.getExtFlag(), po.getExtFlag1());
        });
        return new PageResult<>(visualizationResourcePOPageIPage);
    }


    public Page<VisualizationResourcePO> queryVisualizationPage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = V3UserUtil.getUid();
        String type = null;
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            type = request.getType();
        }
        boolean isCommunityMode = CommunityUtils.isCommunityMode();
        boolean isAsc = request.isAsc();
        String keyword = request.getKeyword();

        // Use QueryDSL for database-agnostic queries
        QCoreDatasetGroup dataset = QCoreDatasetGroup.coreDatasetGroup;
        QCoreDatasource datasource = QCoreDatasource.coreDatasource;
        QDataVisualizationInfo visualization = QDataVisualizationInfo.dataVisualizationInfo;
        QCoreOptRecent optRecent = QCoreOptRecent.coreOptRecent;
        QCoreStore store = QCoreStore.coreStore;
        QPerBusiResource perBusiResource = QPerBusiResource.perBusiResource;

        // Build three separate queries for UNION ALL logic
        // 1. Dataset query
        JPAQuery<VisualizationResourcePO> datasetQuery = queryFactory
                .select(Projections.bean(VisualizationResourcePO.class,
                        dataset.id.as("id"),
                        dataset.id.as("resourceId"),
                        dataset.name.as("name"),
                        ExpressionUtils.as(Expressions.constant(0), "extFlag"),
                        ExpressionUtils.as(Expressions.constant(1), "extFlag1"),
                        ExpressionUtils.as(Expressions.constant("dataset"), "type"),
                        dataset.createBy.castToNum(Long.class).as("creator"),
                        optRecent.uid.as("lastEditor"),
                        optRecent.time.as("lastEditTime"),
                        ExpressionUtils.as(store.resourceId.isNotNull(), "favorite")
                ))
                .from(dataset)
                .innerJoin(optRecent).on(dataset.id.eq(optRecent.resourceId).and(optRecent.uid.eq(uid)))
                .leftJoin(store).on(dataset.id.eq(store.resourceId).and(store.uid.eq(uid)))
                .where(dataset.nodeType.eq("dataset"));

        // Apply filters to dataset query
        if (StringUtils.isNotBlank(keyword)) {
            datasetQuery.where(dataset.name.lower().like("%" + keyword.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(type) && !"dataset".equals(type)) {
            // If type filter doesn't match, return empty for this query
            datasetQuery.where(Expressions.FALSE);
        }
        if (isCommunityMode) {
            datasetQuery.where(JPAExpressions
                    .selectOne()
                    .from(perBusiResource)
                    .where(perBusiResource.id.eq(dataset.id))
                    .notExists());
        }

        // 2. Datasource query
        JPAQuery<VisualizationResourcePO> datasourceQuery = queryFactory
                .select(Projections.bean(VisualizationResourcePO.class,
                        datasource.id.as("id"),
                        datasource.id.as("resourceId"),
                        datasource.name.as("name"),
                        ExpressionUtils.as(Expressions.constant(0), "extFlag"),
                        ExpressionUtils.as(Expressions.constant(1), "extFlag1"),
                        ExpressionUtils.as(Expressions.constant("datasource"), "type"),
                        datasource.createBy.castToNum(Long.class).as("creator"),
                        optRecent.uid.as("lastEditor"),
                        optRecent.time.as("lastEditTime"),
                        ExpressionUtils.as(store.resourceId.isNotNull(), "favorite")
                ))
                .from(datasource)
                .innerJoin(optRecent).on(datasource.id.eq(optRecent.resourceId).and(optRecent.uid.eq(uid)))
                .leftJoin(store).on(datasource.id.eq(store.resourceId).and(store.uid.eq(uid)))
                .where(datasource.type.ne("folder"));

        if (StringUtils.isNotBlank(keyword)) {
            datasourceQuery.where(datasource.name.lower().like("%" + keyword.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(type) && !"datasource".equals(type)) {
            datasourceQuery.where(Expressions.FALSE);
        }
        if (isCommunityMode) {
            datasourceQuery.where(JPAExpressions
                    .selectOne()
                    .from(perBusiResource)
                    .where(perBusiResource.id.eq(datasource.id))
                    .notExists());
        }

        // 3. Visualization query (screen/panel)
        JPAQuery<VisualizationResourcePO> visualizationQuery = queryFactory
                .select(Projections.bean(VisualizationResourcePO.class,
                        visualization.id.as("id"),
                        visualization.id.as("resourceId"),
                        visualization.name.as("name"),
                        ExpressionUtils.as(
                                Expressions.cases()
                                        .when(visualization.mobileLayout.isTrue()).then(1)
                                        .otherwise(0),
                                "extFlag"),
                        visualization.status.as("extFlag1"),
                        ExpressionUtils.as(
                                Expressions.cases()
                                        .when(visualization.type.eq("dataV")).then("screen")
                                        .otherwise("panel"),
                                "type"),
                        visualization.createBy.castToNum(Long.class).as("creator"),
                        optRecent.uid.as("lastEditor"),
                        optRecent.time.as("lastEditTime"),
                        ExpressionUtils.as(store.resourceId.isNotNull(), "favorite")
                ))
                .from(visualization)
                .innerJoin(optRecent).on(visualization.id.eq(optRecent.resourceId).and(optRecent.uid.eq(uid)))
                .leftJoin(store).on(visualization.id.eq(store.resourceId).and(store.uid.eq(uid)))
                .where(visualization.deleteFlag.eq(false).and(visualization.nodeType.eq("leaf")));

        if (StringUtils.isNotBlank(keyword)) {
            visualizationQuery.where(visualization.name.lower().like("%" + keyword.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(type) && !"screen".equals(type) && !"panel".equals(type)) {
            visualizationQuery.where(Expressions.FALSE);
        } else if ("screen".equals(type)) {
            visualizationQuery.where(visualization.type.eq("dataV"));
        } else if ("panel".equals(type)) {
            visualizationQuery.where(visualization.type.ne("dataV"));
        }
        if (isCommunityMode) {
            visualizationQuery.where(JPAExpressions
                    .selectOne()
                    .from(perBusiResource)
                    .where(perBusiResource.id.eq(visualization.id))
                    .notExists());
        }

        // Execute all three queries and merge results
        List<VisualizationResourcePO> datasetResults = datasetQuery.fetch();
        List<VisualizationResourcePO> datasourceResults = datasourceQuery.fetch();
        List<VisualizationResourcePO> visualizationResults = visualizationQuery.fetch();

        // Merge all results
        List<VisualizationResourcePO> allResults = new ArrayList<>();
        allResults.addAll(datasetResults);
        allResults.addAll(datasourceResults);
        allResults.addAll(visualizationResults);

        // Sort by lastEditTime
        allResults.sort((a, b) -> {
            Long timeA = a.getLastEditTime() != null ? a.getLastEditTime() : 0L;
            Long timeB = b.getLastEditTime() != null ? b.getLastEditTime() : 0L;
            return isAsc ? timeA.compareTo(timeB) : timeB.compareTo(timeA);
        });

        // Apply pagination
        int total = allResults.size();
        if (total == 0) {
            return Page.empty();
        }

        int start = (goPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<VisualizationResourcePO> pageResults = allResults.subList(start, end);

        return new PageImpl<>(pageResults, PageRequest.of(goPage - 1, pageSize), total);
    }

    @Transactional
    public void removeSnapshot(Long dvId) {
        if (dvId != null) {
            // 清理历史数据
            Set<Long> dvIds = new HashSet<>();
            dvIds.add(dvId);
            snapshotDataVisualizationInfoRepository.deleteAllByIdInBatch(dvIds);
            snapshotCoreChartViewRepository.deleteBySceneId(dvId);

            deleteViewLinkageFieldSnapshot(dvId, null);
            deleteViewLinkageSnapshot(dvId, null);

            QSnapshotVisualizationLinkJump snapshotVisualizationLinkJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
            QSnapshotVisualizationLinkJumpInfo snapshotVisualizationLinkJumpInfo = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
            List<Long> linkJumpInfoIds = queryFactory.select(snapshotVisualizationLinkJumpInfo.id).from(snapshotVisualizationLinkJumpInfo)
                    .join(snapshotVisualizationLinkJump).on(snapshotVisualizationLinkJumpInfo.linkJumpId.eq(snapshotVisualizationLinkJump.id))
                    .where(snapshotVisualizationLinkJump.sourceDvId.eq(dvId))
                    .fetch();
            if (CollectionUtils.isEmpty(linkJumpInfoIds)) {
                snapshotVisualizationLinkJumpTargetViewInfoRepository.deleteByLinkJumpInfoIds(linkJumpInfoIds);
            }

            List<Long> ids = queryFactory.select(snapshotVisualizationLinkJump.id).from(snapshotVisualizationLinkJump)
                    .where(snapshotVisualizationLinkJump.sourceDvId.eq(dvId)).fetch();
            if (CollectionUtils.isNotEmpty(ids)) {
                snapshotVisualizationLinkJumpInfoRepository.deleteByLinkJumpIds(ids);
            }
            snapshotVisualizationLinkJumpRepository.deleteBySourceDvId(dvId);

            QSnapshotVisualizationOuterParamsInfo snapshotVisualizationOuterParamsInfo = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;
            QSnapshotVisualizationOuterParams snapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
            QSnapshotVisualizationOuterParamsTargetViewInfo snapshotVisualizationOuterParamsTargetViewInfo = QSnapshotVisualizationOuterParamsTargetViewInfo.snapshotVisualizationOuterParamsTargetViewInfo;
            List<String> paramsInfoIds = queryFactory.select(snapshotVisualizationOuterParamsTargetViewInfo.targetId).from(snapshotVisualizationOuterParamsTargetViewInfo)
                    .innerJoin(snapshotVisualizationOuterParamsInfo).on(snapshotVisualizationOuterParamsTargetViewInfo.paramsInfoId.eq(snapshotVisualizationOuterParamsInfo.paramsInfoId))
                    .innerJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParamsInfo.paramsId.eq(snapshotVisualizationOuterParams.paramsId))
                    .where(snapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch();

            if (CollectionUtils.isNotEmpty(paramsInfoIds)) {
                snapshotVisualizationOuterParamsTargetViewInfoRepository.deleteByParamsInfoIds(paramsInfoIds);
            }

            List<String> paramsIds = queryFactory.select(snapshotVisualizationOuterParamsInfo.paramsId).from(snapshotVisualizationOuterParamsInfo)
                    .innerJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParamsInfo.paramsId.eq(snapshotVisualizationOuterParams.paramsId))
                    .where(snapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(dvId)))
                    .fetch();
            if (CollectionUtils.isNotEmpty(paramsIds)) {
                snapshotVisualizationOuterParamsInfoRepository.deleteByParamsIds(paramsIds);
            }
            snapshotVisualizationOuterParamsRepository.deleteByVisualizationId(String.valueOf(dvId));
            //xpack 阈值告警
            chartViewManege.removeThreshold(dvId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);

        }
    }

    public void deleteViewLinkageFieldSnapshot(Long dvId, Long sourceViewId) {
        QSnapshotVisualizationLinkage snapshotVisualizationLinkage = QSnapshotVisualizationLinkage.snapshotVisualizationLinkage;
        QSnapshotVisualizationLinkageField snapshotVisualizationLinkageField = QSnapshotVisualizationLinkageField.snapshotVisualizationLinkageField;

        List<Long> linkageIds = queryFactory.select(snapshotVisualizationLinkage.id)
                .from(snapshotVisualizationLinkage)
                .where(
                        sourceViewId == null
                                ? snapshotVisualizationLinkage.dvId.eq(dvId)
                                : snapshotVisualizationLinkage.dvId.eq(dvId).and(snapshotVisualizationLinkage.sourceViewId.eq(sourceViewId))
                )
                .fetch();
        if (CollectionUtils.isEmpty(linkageIds)) {
            return;
        }
        queryFactory.delete(snapshotVisualizationLinkageField)
                .where(snapshotVisualizationLinkageField.linkageId.in(linkageIds))
                .execute();
    }

    public void deleteViewLinkageSnapshot(Long dvId, Long sourceViewId) {
        QSnapshotVisualizationLinkage snapshotVisualizationLinkage = QSnapshotVisualizationLinkage.snapshotVisualizationLinkage;
        queryFactory.delete(snapshotVisualizationLinkage)
                .where(
                        sourceViewId == null
                                ? snapshotVisualizationLinkage.dvId.eq(dvId)
                                : snapshotVisualizationLinkage.dvId.eq(dvId).and(snapshotVisualizationLinkage.sourceViewId.eq(sourceViewId))
                )
                .execute();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeDvCore(Long dvId) {
        if (dvId != null) {
            // 清理历史数据（主表 data_visualization_info 不做删除，仅由 dvRestore 原地更新，
            // 避免删除后残留的托管实体在刷新时对已删除行发起 UPDATE，导致 ObjectOptimisticLockingFailureException）
            coreChartViewRepository.deleteBySceneId(dvId);
            List<VisualizationLinkage> visualizationLinkages = visualizationLinkageRepository.findByDvId(dvId);
            if (CollectionUtils.isNotEmpty(visualizationLinkages)) {
                visualizationLinkageFieldRepository.deleteByLinkageIds(visualizationLinkages.stream().map(VisualizationLinkage::getId).collect(Collectors.toList()));
            }
            QVisualizationLinkage qVisualizationLinkage = QVisualizationLinkage.visualizationLinkage;
            List<Long> linkageIds = queryFactory.select(qVisualizationLinkage.id)
                    .from(qVisualizationLinkage)
                    .where(qVisualizationLinkage.dvId.eq(dvId))
                    .fetch();
            if (CollectionUtils.isEmpty(linkageIds)) {
                return;
            }
            QVisualizationLinkageField qVisualizationLinkageField = QVisualizationLinkageField.visualizationLinkageField;
            queryFactory.delete(qVisualizationLinkageField)
                    .where(qVisualizationLinkageField.linkageId.in(linkageIds))
                    .execute();
            visualizationLinkageRepository.deleteByDvId(dvId);

            QSnapshotVisualizationLinkJumpInfo snapshotVisualizationLinkJumpInfo = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
            QSnapshotVisualizationLinkJump snapshotVisualizationLinkJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
            List<Long> linkJumpInfoIds = queryFactory.select(snapshotVisualizationLinkJumpInfo.id).from(snapshotVisualizationLinkJumpInfo)
                    .join(snapshotVisualizationLinkJump).on(snapshotVisualizationLinkJumpInfo.linkJumpId.eq(snapshotVisualizationLinkJump.id))
                    .where(snapshotVisualizationLinkJump.sourceDvId.eq(dvId))
                    .fetch();

            if (CollectionUtils.isNotEmpty(linkJumpInfoIds)) {
                snapshotVisualizationLinkJumpTargetViewInfoRepository.deleteByLinkJumpInfoIds(linkJumpInfoIds);
            }
            QVisualizationLinkJump linkJump = QVisualizationLinkJump.visualizationLinkJump;
            List<Long> linkJumpIds = queryFactory.select(linkJump.id).from(linkJump)
                    .where(linkJump.sourceDvId.eq(dvId)).fetch();
            if (CollectionUtils.isNotEmpty(linkJumpIds)) {
                visualizationLinkJumpInfoRepository.deleteByLinkJumpIds(linkJumpIds);
            }

            visualizationLinkJumpRepository.deleteBySourceDvId(dvId);
            QSnapshotVisualizationOuterParamsTargetViewInfo snapshotVisualizationOuterParamsTargetViewInfo = QSnapshotVisualizationOuterParamsTargetViewInfo.snapshotVisualizationOuterParamsTargetViewInfo;
            QSnapshotVisualizationOuterParamsInfo snapshotVisualizationOuterParamsInfo = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;
            QSnapshotVisualizationOuterParams snapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;

            List<String> paramsInfoIds = queryFactory.select(snapshotVisualizationOuterParamsTargetViewInfo.targetId)
                    .from(snapshotVisualizationOuterParamsTargetViewInfo)
                    .innerJoin(snapshotVisualizationOuterParamsInfo).on(snapshotVisualizationOuterParamsTargetViewInfo.paramsInfoId.eq(snapshotVisualizationOuterParamsInfo.paramsInfoId))
                    .innerJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParamsInfo.paramsId.eq(snapshotVisualizationOuterParams.paramsId))
                    .where(snapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch();
            if (CollectionUtils.isNotEmpty(paramsInfoIds)) {
                snapshotVisualizationOuterParamsTargetViewInfoRepository.deleteByParamsInfoIds(paramsInfoIds);
            }

            QVisualizationOuterParamsInfo visualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
            QVisualizationOuterParams visualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;
            List<String> paramsIds = queryFactory.select(visualizationOuterParamsInfo.paramsId).from(visualizationOuterParamsInfo)
                    .leftJoin(visualizationOuterParams).on(visualizationOuterParamsInfo.paramsId.eq(visualizationOuterParams.paramsId))
                    .where(visualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch();

            if (CollectionUtils.isNotEmpty(paramsIds)) {
                visualizationOuterParamsInfoRepository.deleteByParamsIds(paramsIds);
            }
            visualizationOuterParamsRepository.deleteByVisualizationId(dvId);
            //xpack 阈值告警
            chartViewManege.removeThreshold(dvId, CommonConstants.RESOURCE_TABLE.CORE);
        }
    }

    @Transactional
    public void dvSnapshotRecover(Long dvId) {
        // 清理历史数据
        CoreVisualizationManage proxy = CommonBeanFactory.proxy(this.getClass());
        assert proxy != null;
        proxy.removeSnapshot(dvId);
        dataVisualizationInfoRepository.findById(dvId).ifPresent(visualizationInfo -> {
            SnapshotDataVisualizationInfo snapshotDataVisualizationInfo = new SnapshotDataVisualizationInfo();
            BeanUtils.copyBean(snapshotDataVisualizationInfo, visualizationInfo);
            snapshotDataVisualizationInfoRepository.saveAndFlush(snapshotDataVisualizationInfo);
        });

        coreChartViewRepository.findBySceneId(dvId).forEach(item -> {
            SnapshotCoreChartView snapshotCoreChartView = new SnapshotCoreChartView();
            BeanUtils.copyBean(snapshotCoreChartView, item);
            snapshotCoreChartViewRepository.saveAndFlush(snapshotCoreChartView);
        });

        QVisualizationLinkJumpInfo visualizationLinkJumpInfo = QVisualizationLinkJumpInfo.visualizationLinkJumpInfo;
        QVisualizationLinkJump visualizationLinkJump = QVisualizationLinkJump.visualizationLinkJump;
        QVisualizationLinkJumpTargetViewInfo visualizationLinkJumpTargetViewInfo = QVisualizationLinkJumpTargetViewInfo.visualizationLinkJumpTargetViewInfo;
        queryFactory.select(Projections.fields(VisualizationLinkJumpTargetViewInfo.class,
                        visualizationLinkJumpTargetViewInfo.targetId,
                        visualizationLinkJumpTargetViewInfo.linkJumpInfoId,
                        visualizationLinkJumpTargetViewInfo.sourceFieldActiveId,
                        visualizationLinkJumpTargetViewInfo.targetViewId,
                        visualizationLinkJumpTargetViewInfo.targetFieldId,
                        visualizationLinkJumpTargetViewInfo.copyFrom,
                        visualizationLinkJumpTargetViewInfo.copyId,
                        visualizationLinkJumpTargetViewInfo.targetType)).from(visualizationLinkJumpTargetViewInfo)
                .join(visualizationLinkJumpInfo).on(visualizationLinkJumpTargetViewInfo.linkJumpInfoId.eq(visualizationLinkJumpInfo.id))
                .join(visualizationLinkJump).on(visualizationLinkJumpInfo.linkJumpId.eq(visualizationLinkJump.id))
                .where(visualizationLinkJump.sourceDvId.eq(dvId)).fetch().forEach(item -> {
                    SnapshotVisualizationLinkJumpTargetViewInfo snapshotVisualizationLinkJumpTargetViewInfo = new SnapshotVisualizationLinkJumpTargetViewInfo();
                    BeanUtils.copyBean(snapshotVisualizationLinkJumpTargetViewInfo, item);
                    snapshotVisualizationLinkJumpTargetViewInfoRepository.saveAndFlush(snapshotVisualizationLinkJumpTargetViewInfo);
                });


        queryFactory.select(Projections.fields(VisualizationLinkJumpInfo.class,
                        visualizationLinkJumpInfo.id,
                        visualizationLinkJumpInfo.linkJumpId,
                        visualizationLinkJumpInfo.linkType,
                        visualizationLinkJumpInfo.jumpType,
                        visualizationLinkJumpInfo.targetDvId,
                        visualizationLinkJumpInfo.sourceFieldId,
                        visualizationLinkJumpInfo.content,
                        visualizationLinkJumpInfo.checked,
                        visualizationLinkJumpInfo.attachParams,
                        visualizationLinkJumpInfo.copyFrom,
                        visualizationLinkJumpInfo.copyId,
                        visualizationLinkJumpInfo.windowSize
                )).from(visualizationLinkJumpInfo).join(visualizationLinkJump).on(visualizationLinkJumpInfo.linkJumpId.eq(visualizationLinkJump.id))
                .where(visualizationLinkJump.sourceDvId.eq(dvId)).fetch().forEach(item -> {
                    SnapshotVisualizationLinkJumpInfo snapshotVisualizationLinkJumpInfo = new SnapshotVisualizationLinkJumpInfo();
                    BeanUtils.copyBean(snapshotVisualizationLinkJumpInfo, item);
                    snapshotVisualizationLinkJumpInfoRepository.saveAndFlush(snapshotVisualizationLinkJumpInfo);
                });


        visualizationLinkJumpRepository.findBySourceDvId(dvId).forEach(item -> {
            SnapshotVisualizationLinkJump snapshotVisualizationLinkJump = new SnapshotVisualizationLinkJump();
            BeanUtils.copyBean(snapshotVisualizationLinkJump, item);
            snapshotVisualizationLinkJumpRepository.saveAndFlush(snapshotVisualizationLinkJump);
        });


        QVisualizationLinkageField visualizationLinkageField = QVisualizationLinkageField.visualizationLinkageField;
        QVisualizationLinkage visualizationLinkage = QVisualizationLinkage.visualizationLinkage;
        queryFactory.select(Projections.fields(VisualizationLinkageField.class,
                        visualizationLinkageField.id,
                        visualizationLinkageField.linkageId,
                        visualizationLinkageField.sourceField,
                        visualizationLinkageField.targetField,
                        visualizationLinkageField.updateTime,
                        visualizationLinkageField.copyFrom,
                        visualizationLinkageField.copyId)).from(visualizationLinkageField)
                .join(visualizationLinkage).on(visualizationLinkageField.linkageId.eq(visualizationLinkage.id))
                .where(visualizationLinkage.dvId.eq(dvId)).fetch().forEach(item -> {
                    SnapshotVisualizationLinkageField snapshotVisualizationLinkageField = new SnapshotVisualizationLinkageField();
                    BeanUtils.copyBean(snapshotVisualizationLinkageField, item);
                    snapshotVisualizationLinkageFieldRepository.saveAndFlush(snapshotVisualizationLinkageField);
                });

        visualizationLinkageRepository.findByDvId(dvId).forEach(item -> {
            SnapshotVisualizationLinkage snapshotVisualizationLinkage = new SnapshotVisualizationLinkage();
            BeanUtils.copyBean(snapshotVisualizationLinkage, item);
            snapshotVisualizationLinkageRepository.saveAndFlush(snapshotVisualizationLinkage);
        });

        QVisualizationOuterParamsInfo visualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
        QVisualizationOuterParams visualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;
        QVisualizationOuterParamsTargetViewInfo visualizationOuterParamsTargetViewInfo = QVisualizationOuterParamsTargetViewInfo.visualizationOuterParamsTargetViewInfo;

        queryFactory.select(Projections.bean(VisualizationOuterParamsTargetViewInfo.class,
                        visualizationOuterParamsTargetViewInfo.targetId,
                        visualizationOuterParamsTargetViewInfo.paramsInfoId,
                        visualizationOuterParamsTargetViewInfo.targetViewId,
                        visualizationOuterParamsTargetViewInfo.targetFieldId,
                        visualizationOuterParamsTargetViewInfo.copyFrom,
                        visualizationOuterParamsTargetViewInfo.copyId,
                        visualizationOuterParamsTargetViewInfo.targetDsId
                )).from(visualizationOuterParamsTargetViewInfo)
                .leftJoin(visualizationOuterParamsInfo).on(visualizationOuterParamsTargetViewInfo.paramsInfoId.eq(visualizationOuterParamsInfo.paramsInfoId))
                .leftJoin(visualizationOuterParams).on(visualizationOuterParams.paramsId.eq(visualizationOuterParamsInfo.paramsId))
                .where(visualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch().forEach(item -> {
                    SnapshotVisualizationOuterParamsTargetViewInfo snapshotVisualizationOuterParamsTargetViewInfo = new SnapshotVisualizationOuterParamsTargetViewInfo();
                    BeanUtils.copyBean(snapshotVisualizationOuterParamsTargetViewInfo, item);
                    snapshotVisualizationOuterParamsTargetViewInfoRepository.saveAndFlush(snapshotVisualizationOuterParamsTargetViewInfo);
                });

        queryFactory.select(Projections.bean(VisualizationOuterParamsInfo.class,
                        visualizationOuterParamsInfo.paramsInfoId,
                        visualizationOuterParamsInfo.paramsId,
                        visualizationOuterParamsInfo.paramName,
                        visualizationOuterParamsInfo.checked,
                        visualizationOuterParamsInfo.copyFrom,
                        visualizationOuterParamsInfo.copyId,
                        visualizationOuterParamsInfo.required,
                        visualizationOuterParamsInfo.defaultValue,
                        visualizationOuterParamsInfo.enabledDefault
                )).from(visualizationOuterParamsInfo)
                .leftJoin(visualizationOuterParams).on(visualizationOuterParams.paramsId.eq(visualizationOuterParamsInfo.paramsId))
                .where(visualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch().forEach(item -> {
                    SnapshotVisualizationOuterParamsInfo snapshotVisualizationOuterParamsInfo = new SnapshotVisualizationOuterParamsInfo();
                    BeanUtils.copyBean(snapshotVisualizationOuterParamsInfo, item);
                    snapshotVisualizationOuterParamsInfoRepository.saveAndFlush(snapshotVisualizationOuterParamsInfo);
                });

        visualizationOuterParamsRepository.findByVisualizationId(String.valueOf(dvId)).forEach(item -> {
            SnapshotVisualizationOuterParams snapshotVisualizationOuterParams = new SnapshotVisualizationOuterParams();
            BeanUtils.copyBean(snapshotVisualizationOuterParams, item);
            snapshotVisualizationOuterParamsRepository.saveAndFlush(snapshotVisualizationOuterParams);
        });
        //xpack 阈值告警
        chartViewManege.restoreThreshold(dvId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
    }

    @Transactional
    public void dvRestore(Long dvId) {
        // 主表行未被删除，此处按 dvId 原地更新（saveAndFlush 命中已存在行做 UPDATE），
        // 将镜像表数据同步到主表
        snapshotDataVisualizationInfoRepository.findById(dvId).ifPresent(item -> {
            DataVisualizationInfo dataVisualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(dataVisualizationInfo, item, "updateTime");
            dataVisualizationInfoRepository.saveAndFlush(dataVisualizationInfo);
        });

        List<CoreChartView> coreChartViews = new ArrayList<>();
        snapshotCoreChartViewRepository.findBySceneId(dvId).forEach(item -> {
            coreChartViews.add(BeanUtils.copyBean(new CoreChartView(), item));
        });
        coreChartViewRepository.saveAllAndFlush(coreChartViews);

        // LinkJump / Linkage / OuterParams 相关主表先按 dvId 清理，再从镜像表复制，避免残留脏数据
        removeCoreLinkAndOuterParams(dvId);

        QSnapshotVisualizationLinkJump snapshotVisualizationLinkJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;
        QSnapshotVisualizationLinkJumpInfo snapshotVisualizationLinkJumpInfo = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
        QSnapshotVisualizationLinkJumpTargetViewInfo snapshotVisualizationLinkJumpTargetViewInfo = QSnapshotVisualizationLinkJumpTargetViewInfo.snapshotVisualizationLinkJumpTargetViewInfo;
        queryFactory.select(Projections.fields(SnapshotVisualizationLinkJumpTargetViewInfo.class,
                        snapshotVisualizationLinkJumpTargetViewInfo.targetId,
                        snapshotVisualizationLinkJumpTargetViewInfo.linkJumpInfoId,
                        snapshotVisualizationLinkJumpTargetViewInfo.sourceFieldActiveId,
                        snapshotVisualizationLinkJumpTargetViewInfo.targetViewId,
                        snapshotVisualizationLinkJumpTargetViewInfo.targetFieldId,
                        snapshotVisualizationLinkJumpTargetViewInfo.copyFrom,
                        snapshotVisualizationLinkJumpTargetViewInfo.copyId,
                        snapshotVisualizationLinkJumpTargetViewInfo.targetType
                )).from(snapshotVisualizationLinkJumpTargetViewInfo)
                .leftJoin(snapshotVisualizationLinkJumpInfo).on(snapshotVisualizationLinkJumpTargetViewInfo.linkJumpInfoId.eq(snapshotVisualizationLinkJumpInfo.id))
                .leftJoin(snapshotVisualizationLinkJump).on(snapshotVisualizationLinkJumpInfo.linkJumpId.eq(snapshotVisualizationLinkJump.id))
                .where(snapshotVisualizationLinkJump.sourceDvId.eq(dvId)).fetch().forEach(item -> {
                    VisualizationLinkJumpTargetViewInfo visualizationLinkJumpTargetViewInfo = new VisualizationLinkJumpTargetViewInfo();
                    BeanUtils.copyBean(visualizationLinkJumpTargetViewInfo, item);
                    visualizationLinkJumpTargetViewInfoRepository.saveAndFlush(visualizationLinkJumpTargetViewInfo);
                });


        queryFactory.select(Projections.fields(SnapshotVisualizationLinkJumpInfo.class,
                        snapshotVisualizationLinkJumpInfo.id,
                        snapshotVisualizationLinkJumpInfo.linkJumpId,
                        snapshotVisualizationLinkJumpInfo.linkType,
                        snapshotVisualizationLinkJumpInfo.jumpType,
                        snapshotVisualizationLinkJumpInfo.targetDvId,
                        snapshotVisualizationLinkJumpInfo.sourceFieldId,
                        snapshotVisualizationLinkJumpInfo.content,
                        snapshotVisualizationLinkJumpInfo.checked,
                        snapshotVisualizationLinkJumpInfo.attachParams,
                        snapshotVisualizationLinkJumpInfo.copyFrom,
                        snapshotVisualizationLinkJumpInfo.copyId,
                        snapshotVisualizationLinkJumpInfo.windowSize
                )).from(snapshotVisualizationLinkJumpInfo)
                .leftJoin(snapshotVisualizationLinkJump).on(snapshotVisualizationLinkJumpInfo.linkJumpId.eq(snapshotVisualizationLinkJump.id))
                .where(snapshotVisualizationLinkJump.sourceDvId.eq(dvId)).fetch().forEach(item -> {
                    VisualizationLinkJumpInfo visualizationLinkJumpInfo = new VisualizationLinkJumpInfo();
                    BeanUtils.copyBean(visualizationLinkJumpInfo, item);
                    visualizationLinkJumpInfoRepository.saveAndFlush(visualizationLinkJumpInfo);
                });

        queryFactory.select(Projections.fields(SnapshotVisualizationLinkJump.class,
                        snapshotVisualizationLinkJump.id,
                        snapshotVisualizationLinkJump.sourceDvId,
                        snapshotVisualizationLinkJump.sourceViewId,
                        snapshotVisualizationLinkJump.linkJumpInfo,
                        snapshotVisualizationLinkJump.checked,
                        snapshotVisualizationLinkJump.copyFrom,
                        snapshotVisualizationLinkJump.copyId
                )).from(snapshotVisualizationLinkJump)
                .where(snapshotVisualizationLinkJump.sourceDvId.eq(dvId)).fetch().forEach(item -> {
                    VisualizationLinkJump visualizationLinkJump = new VisualizationLinkJump();
                    BeanUtils.copyBean(visualizationLinkJump, item);
                    visualizationLinkJumpRepository.saveAndFlush(visualizationLinkJump);
                });

        QSnapshotVisualizationLinkage snapshotVisualizationLinkage = QSnapshotVisualizationLinkage.snapshotVisualizationLinkage;
        queryFactory.select(Projections.fields(SnapshotVisualizationLinkage.class,
                        snapshotVisualizationLinkage.id,
                        snapshotVisualizationLinkage.dvId,
                        snapshotVisualizationLinkage.sourceViewId,
                        snapshotVisualizationLinkage.targetViewId,
                        snapshotVisualizationLinkage.updateTime,
                        snapshotVisualizationLinkage.updatePeople,
                        snapshotVisualizationLinkage.linkageActive,
                        snapshotVisualizationLinkage.ext1,
                        snapshotVisualizationLinkage.ext2,
                        snapshotVisualizationLinkage.copyFrom,
                        snapshotVisualizationLinkage.copyId
                )).from(snapshotVisualizationLinkage)
                .where(snapshotVisualizationLinkage.dvId.eq(dvId)).fetch().forEach(itme -> {
                    VisualizationLinkage visualizationLinkage = new VisualizationLinkage();
                    BeanUtils.copyBean(visualizationLinkage, itme);
                    visualizationLinkageRepository.saveAndFlush(visualizationLinkage);
                });

        QSnapshotVisualizationLinkageField snapshotVisualizationLinkageField = QSnapshotVisualizationLinkageField.snapshotVisualizationLinkageField;

        queryFactory.select(Projections.fields(SnapshotVisualizationLinkageField.class,
                        snapshotVisualizationLinkageField.id,
                        snapshotVisualizationLinkageField.linkageId,
                        snapshotVisualizationLinkageField.sourceField,
                        snapshotVisualizationLinkageField.targetField,
                        snapshotVisualizationLinkageField.updateTime,
                        snapshotVisualizationLinkageField.copyFrom,
                        snapshotVisualizationLinkageField.copyId
                )).from(snapshotVisualizationLinkageField)
                .leftJoin(snapshotVisualizationLinkage).on(snapshotVisualizationLinkageField.linkageId.eq(snapshotVisualizationLinkage.id))
                .where(snapshotVisualizationLinkage.dvId.eq(dvId)).fetch().forEach(item -> {
                    VisualizationLinkageField visualizationLinkageField = new VisualizationLinkageField();
                    BeanUtils.copyBean(visualizationLinkageField, item);
                    visualizationLinkageFieldRepository.saveAndFlush(visualizationLinkageField);
                });


        QSnapshotVisualizationOuterParamsInfo snapshotVisualizationOuterParamsInfo = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;
        QSnapshotVisualizationOuterParams snapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
        QSnapshotVisualizationOuterParamsTargetViewInfo snapshotVisualizationOuterParamsTargetViewInfo = QSnapshotVisualizationOuterParamsTargetViewInfo.snapshotVisualizationOuterParamsTargetViewInfo;
        queryFactory.select(Projections.fields(SnapshotVisualizationOuterParamsTargetViewInfo.class,
                        snapshotVisualizationOuterParamsTargetViewInfo.targetId,
                        snapshotVisualizationOuterParamsTargetViewInfo.paramsInfoId,
                        snapshotVisualizationOuterParamsTargetViewInfo.targetViewId,
                        snapshotVisualizationOuterParamsTargetViewInfo.targetFieldId,
                        snapshotVisualizationOuterParamsTargetViewInfo.copyFrom,
                        snapshotVisualizationOuterParamsTargetViewInfo.copyId,
                        snapshotVisualizationOuterParamsTargetViewInfo.targetDsId,
                        snapshotVisualizationOuterParamsTargetViewInfo.matchMode
                )).from(snapshotVisualizationOuterParamsTargetViewInfo)
                .leftJoin(snapshotVisualizationOuterParamsInfo).on(snapshotVisualizationOuterParamsTargetViewInfo.paramsInfoId.eq(snapshotVisualizationOuterParamsInfo.paramsInfoId))
                .leftJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParams.paramsId.eq(snapshotVisualizationOuterParamsInfo.paramsId))
                .where(snapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch().forEach(item -> {
                    VisualizationOuterParamsTargetViewInfo visualizationOuterParamsTargetViewInfo = new VisualizationOuterParamsTargetViewInfo();
                    BeanUtils.copyBean(visualizationOuterParamsTargetViewInfo, item);
                    visualizationOuterParamsTargetViewInfoRepository.saveAndFlush(visualizationOuterParamsTargetViewInfo);
                });


        queryFactory.select(Projections.fields(SnapshotVisualizationOuterParamsInfo.class,
                        snapshotVisualizationOuterParamsInfo.paramsInfoId,
                        snapshotVisualizationOuterParamsInfo.paramsId,
                        snapshotVisualizationOuterParamsInfo.paramName,
                        snapshotVisualizationOuterParamsInfo.checked,
                        snapshotVisualizationOuterParamsInfo.copyFrom,
                        snapshotVisualizationOuterParamsInfo.copyId,
                        snapshotVisualizationOuterParamsInfo.required,
                        snapshotVisualizationOuterParamsInfo.defaultValue,
                        snapshotVisualizationOuterParamsInfo.enabledDefault
                )).from(snapshotVisualizationOuterParamsInfo)
                .leftJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParamsInfo.paramsId.eq(snapshotVisualizationOuterParams.paramsId))
                .where(snapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch().forEach(item -> {
                    VisualizationOuterParamsInfo visualizationOuterParamsInfo = new VisualizationOuterParamsInfo();
                    BeanUtils.copyBean(visualizationOuterParamsInfo, item);
                    visualizationOuterParamsInfoRepository.saveAndFlush(visualizationOuterParamsInfo);

                });

        queryFactory.select(Projections.fields(SnapshotVisualizationOuterParams.class,
                snapshotVisualizationOuterParams.paramsId,
                snapshotVisualizationOuterParams.visualizationId,
                snapshotVisualizationOuterParams.checked,
                snapshotVisualizationOuterParams.remark,
                snapshotVisualizationOuterParams.copyFrom,
                snapshotVisualizationOuterParams.copyId
        )).from(snapshotVisualizationOuterParams).where(snapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch().forEach(item -> {
            VisualizationOuterParams outerParams = new VisualizationOuterParams();
            BeanUtils.copyBean(outerParams, item);
            visualizationOuterParamsRepository.saveAndFlush(outerParams);
        });


        //xpack 阈值告警
        chartViewManege.restoreThreshold(dvId, CommonConstants.RESOURCE_TABLE.CORE);
    }

    /**
     * 恢复镜像前，按 dvId 清理主表中 LinkJump / Linkage / OuterParams 相关数据
     * 先删子表再删父表，避免残留脏数据
     */
    private void removeCoreLinkAndOuterParams(Long dvId) {
        // LinkJump: TargetViewInfo -> Info -> LinkJump
        QVisualizationLinkJump visualizationLinkJump = QVisualizationLinkJump.visualizationLinkJump;
        QVisualizationLinkJumpInfo visualizationLinkJumpInfo = QVisualizationLinkJumpInfo.visualizationLinkJumpInfo;
        List<Long> linkJumpIds = queryFactory.select(visualizationLinkJump.id).from(visualizationLinkJump)
                .where(visualizationLinkJump.sourceDvId.eq(dvId)).fetch();
        if (CollectionUtils.isNotEmpty(linkJumpIds)) {
            List<Long> linkJumpInfoIds = queryFactory.select(visualizationLinkJumpInfo.id).from(visualizationLinkJumpInfo)
                    .where(visualizationLinkJumpInfo.linkJumpId.in(linkJumpIds)).fetch();
            if (CollectionUtils.isNotEmpty(linkJumpInfoIds)) {
                visualizationLinkJumpTargetViewInfoRepository.deleteByLinkJumpInfoIds(linkJumpInfoIds);
            }
            visualizationLinkJumpInfoRepository.deleteByLinkJumpIds(linkJumpIds);
        }
        visualizationLinkJumpRepository.deleteBySourceDvId(dvId);

        // Linkage: LinkageField -> Linkage
        QVisualizationLinkage visualizationLinkage = QVisualizationLinkage.visualizationLinkage;
        List<Long> linkageIds = queryFactory.select(visualizationLinkage.id).from(visualizationLinkage)
                .where(visualizationLinkage.dvId.eq(dvId)).fetch();
        if (CollectionUtils.isNotEmpty(linkageIds)) {
            visualizationLinkageFieldRepository.deleteByLinkageIds(linkageIds);
        }
        visualizationLinkageRepository.deleteByDvId(dvId);

        // OuterParams: TargetViewInfo -> Info -> OuterParams
        QVisualizationOuterParams visualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;
        QVisualizationOuterParamsInfo visualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
        List<String> paramsIds = queryFactory.select(visualizationOuterParams.paramsId).from(visualizationOuterParams)
                .where(visualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch();
        if (CollectionUtils.isNotEmpty(paramsIds)) {
            List<String> paramsInfoIds = queryFactory.select(visualizationOuterParamsInfo.paramsInfoId).from(visualizationOuterParamsInfo)
                    .where(visualizationOuterParamsInfo.paramsId.in(paramsIds)).fetch();
            if (CollectionUtils.isNotEmpty(paramsInfoIds)) {
                visualizationOuterParamsTargetViewInfoRepository.deleteByParamsInfoIds(paramsInfoIds);
            }
            visualizationOuterParamsInfoRepository.deleteByParamsIds(paramsIds);
        }
        visualizationOuterParamsRepository.deleteByVisualizationId(dvId);
    }

    public List<VisualizationViewTableDTO> getVisualizationViewDetails(Long dvId) {
        QCoreChartView ccv = QCoreChartView.coreChartView;
        QCoreDatasetTableField field = QCoreDatasetTableField.coreDatasetTableField;

        return queryFactory
                .select(Projections.fields(
                        VisualizationViewTableDTO.class,
                        ccv.id,
                        ccv.title,
                        ccv.sceneId,
                        ccv.tableId,
                        ccv.type,
                        ccv.render,
                        Projections.fields(
                                DatasetTableFieldDTO.class,
                                field.id.as("id"),
                                field.originName.as("originName"),
                                field.name.as("name"),
                                field.type.as("type"),
                                field.deType.as("deType")
                        )
                ))
                .from(ccv)
                .leftJoin(field).on(ccv.tableId.eq(field.datasetGroupId))
                .where(ccv.sceneId.eq(dvId)
                        .and(ccv.id.isNotNull())
                        .and(ccv.type.ne("VQuery")))
                .fetch();
    }

    public DataVisualizationVO findDvInfo(Long dvId, String dvType, String resourceTable) {
        if ("snapshot".equals(resourceTable)) {
            return findSnapshotDvInfo(dvId, dvType);
        } else {
            return findNormalDvInfo(dvId, dvType);
        }
    }

    public DataVisualizationVO findNormalDvInfo(Long dvId, String dvType) {
        DataVisualizationInfo entity = dataVisualizationInfoRepository.findDvInfoEntity(dvId, dvType)
                .orElseThrow(() -> new EntityNotFoundException("Data Visualization not found with id: " + dvId));
        DataVisualizationVO vo = new DataVisualizationVO();
        BeanUtils.copyBean(vo, entity);
        return vo;
    }

    public DataVisualizationVO findSnapshotDvInfo(Long dvId, String dvType) {
        SnapshotDataVisualizationInfo entity = snapshotDataVisualizationInfoRepository.findSnapshotDvInfoEntity(dvId, dvType)
                .orElseThrow(() -> new EntityNotFoundException("Snapshot Data Visualization not found with id: " + dvId));
        DataVisualizationVO vo = new DataVisualizationVO();
        BeanUtils.copyBean(vo, entity);
        return vo;
    }

    public List<VisualizationReportFilterVO> queryReportFilter(Long dvId, Long taskId) {
        // 1. 查询实体列表
        List<VisualizationReportFilter> entities = reportFilterRepository
                .findByResourceIdAndTaskId(dvId, taskId);
        // 2. 转换为VO列表
        return entities.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    private VisualizationReportFilterVO convertToVo(VisualizationReportFilter entity) {
        VisualizationReportFilterVO vo = new VisualizationReportFilterVO();
        BeanUtils.copyBean(vo, entity);
        return vo;
    }

}

