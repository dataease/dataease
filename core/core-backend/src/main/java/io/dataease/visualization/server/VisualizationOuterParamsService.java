package io.dataease.visualization.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.dataset.vo.CoreDatasetGroupVO;
import io.dataease.api.dataset.vo.CoreDatasetTableFieldVO;
import io.dataease.api.visualization.VisualizationOuterParamsApi;
import io.dataease.api.visualization.dto.VisualizationOuterParamsDTO;
import io.dataease.api.visualization.dto.VisualizationOuterParamsInfoDTO;
import io.dataease.api.visualization.response.VisualizationOuterParamsBaseResponse;
import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.CoreDatasetTable;
import io.dataease.dao.auto.entity.QCoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableRepository;
import io.dataease.constant.DeTypeConstants;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2024/3/11 09:44
 */
@RestController
@RequestMapping("outerParams")
public class VisualizationOuterParamsService implements VisualizationOuterParamsApi {

    @Resource
    private JPAQueryFactory queryFactory;
    @Resource
    private SnapshotVisualizationOuterParamsRepository snapshotVisualizationOuterParamsRepository;
    @Resource
    private SnapshotVisualizationOuterParamsInfoRepository snapshotVisualizationOuterParamsInfoRepository;
    @Resource
    private SnapshotVisualizationOuterParamsTargetViewInfoRepository snapshotVisualizationOuterParamsTargetViewInfoRepository;
    @Resource
    private CoreDatasetTableRepository coreDatasetTableRepository;
    @Autowired
    private DataVisualizationServer dataVisualizationServer;


    @Override
    public VisualizationOuterParamsDTO queryWithVisualizationId(String visualizationId) {
        QSnapshotDataVisualizationInfo qSnapshotDataVisualizationInfo = QSnapshotDataVisualizationInfo.snapshotDataVisualizationInfo;
        QSnapshotVisualizationOuterParams qSnapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
        VisualizationOuterParamsDTO visualizationOuterParamsDTO = queryFactory.select(Projections.fields(VisualizationOuterParamsDTO.class,
                        qSnapshotDataVisualizationInfo.id.as("visualizationId"),
                        qSnapshotVisualizationOuterParams.checked.as("checked")
                )).from(qSnapshotDataVisualizationInfo)
                .leftJoin(qSnapshotVisualizationOuterParams).on(qSnapshotDataVisualizationInfo.id.eq(qSnapshotVisualizationOuterParams.visualizationId))
                .where(qSnapshotDataVisualizationInfo.id.eq(visualizationId)).fetchFirst();
        if (visualizationOuterParamsDTO != null && visualizationOuterParamsDTO.getChecked() == null) {
            visualizationOuterParamsDTO.setChecked(false);
        }
        return visualizationOuterParamsDTO;
    }

    @Override
    public void updateOuterParamsSet(VisualizationOuterParamsDTO outerParamsDTO) {
        String visualizationId = outerParamsDTO.getVisualizationId();
        Assert.notNull(visualizationId, "visualizationId cannot be null");
        Map<String, String> paramsInfoNameIdMap = new HashMap<>();
        QSnapshotVisualizationOuterParamsInfo qSnapshotVisualizationOuterParamsInfo = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;
        QSnapshotVisualizationOuterParams qSnapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
        List<SnapshotVisualizationOuterParamsInfo> paramsInfoNameIdList = queryFactory.select(Projections.fields(SnapshotVisualizationOuterParamsInfo.class,
                        qSnapshotVisualizationOuterParamsInfo.paramName,
                        qSnapshotVisualizationOuterParamsInfo.paramsInfoId
                )).from(qSnapshotVisualizationOuterParams)
                .innerJoin(qSnapshotVisualizationOuterParamsInfo).on(qSnapshotVisualizationOuterParams.paramsId.eq(qSnapshotVisualizationOuterParamsInfo.paramsId))
                .where(qSnapshotVisualizationOuterParams.visualizationId.eq(visualizationId)).fetch();
        if (!CollectionUtils.isEmpty(paramsInfoNameIdList)) {
            paramsInfoNameIdMap = paramsInfoNameIdList.stream()
                    .collect(Collectors.toMap(SnapshotVisualizationOuterParamsInfo::getParamName, SnapshotVisualizationOuterParamsInfo::getParamsInfoId));
        }
        //清理原有数据
        QSnapshotVisualizationOuterParamsInfo snapshotVisualizationOuterParamsInfo = QSnapshotVisualizationOuterParamsInfo.snapshotVisualizationOuterParamsInfo;
        QSnapshotVisualizationOuterParams snapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
        QSnapshotVisualizationOuterParamsTargetViewInfo snapshotVisualizationOuterParamsTargetViewInfo = QSnapshotVisualizationOuterParamsTargetViewInfo.snapshotVisualizationOuterParamsTargetViewInfo;
        List<String> paramsInfoIds = queryFactory.select(snapshotVisualizationOuterParamsTargetViewInfo.targetId).from(snapshotVisualizationOuterParamsTargetViewInfo)
                .innerJoin(snapshotVisualizationOuterParamsInfo).on(snapshotVisualizationOuterParamsTargetViewInfo.paramsInfoId.eq(snapshotVisualizationOuterParamsInfo.paramsInfoId))
                .innerJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParamsInfo.paramsId.eq(snapshotVisualizationOuterParams.paramsId))
                .where(snapshotVisualizationOuterParams.visualizationId.eq(visualizationId)).fetch();

        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(paramsInfoIds)) {
            snapshotVisualizationOuterParamsTargetViewInfoRepository.deleteByParamsInfoIds(paramsInfoIds);
        }

        List<String> paramsIds = queryFactory.select(snapshotVisualizationOuterParamsInfo.paramsId).from(snapshotVisualizationOuterParamsInfo)
                .innerJoin(snapshotVisualizationOuterParams).on(snapshotVisualizationOuterParamsInfo.paramsId.eq(snapshotVisualizationOuterParams.paramsId))
                .where(snapshotVisualizationOuterParams.visualizationId.eq(visualizationId))
                .fetch();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(paramsIds)) {
            snapshotVisualizationOuterParamsInfoRepository.deleteByParamsIds(paramsIds);
        }
        snapshotVisualizationOuterParamsRepository.deleteByVisualizationId(visualizationId);
        if (CollectionUtils.isEmpty(outerParamsDTO.getOuterParamsInfoArray())) {
            return;
        }
        // 插入新的数据
        String paramsId = UUID.randomUUID().toString();
        outerParamsDTO.setParamsId(paramsId);
        SnapshotVisualizationOuterParams newOuterParams = new SnapshotVisualizationOuterParams();
        BeanUtils.copyBean(newOuterParams, outerParamsDTO);
        snapshotVisualizationOuterParamsRepository.saveAndFlush(newOuterParams);
        Map<String, String> finalParamsInfoNameIdMap = paramsInfoNameIdMap;
        Optional.ofNullable(outerParamsDTO.getOuterParamsInfoArray()).orElse(new ArrayList<>()).forEach(outerParamsInfo -> {
            String paramsInfoId = finalParamsInfoNameIdMap.get(outerParamsInfo.getParamName());
            if (StringUtils.isEmpty(paramsInfoId)) {
                paramsInfoId = UUID.randomUUID().toString();
            }
            outerParamsInfo.setParamsInfoId(paramsInfoId);
            outerParamsInfo.setParamsId(paramsId);
            SnapshotVisualizationOuterParamsInfo newOuterParamsInfo = new SnapshotVisualizationOuterParamsInfo();
            BeanUtils.copyBean(newOuterParamsInfo, outerParamsInfo);
            snapshotVisualizationOuterParamsInfoRepository.saveAndFlush(newOuterParamsInfo);
            String finalParamsInfoId = paramsInfoId;
            Optional.ofNullable(outerParamsInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = UUID.randomUUID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setParamsInfoId(finalParamsInfoId);
                SnapshotVisualizationOuterParamsTargetViewInfo newOuterParamsTargetViewInfo = new SnapshotVisualizationOuterParamsTargetViewInfo();
                BeanUtils.copyBean(newOuterParamsTargetViewInfo, targetViewInfo);
                snapshotVisualizationOuterParamsTargetViewInfoRepository.saveAndFlush(newOuterParamsTargetViewInfo);
            });
        });

    }

    @DeLinkPermit
    @Override
    public VisualizationOuterParamsBaseResponse getOuterParamsInfo(String visualizationId) {

        QVisualizationOuterParams qVisualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;
        QVisualizationOuterParamsInfo qVisualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
        QVisualizationOuterParamsTargetViewInfo visualizationOuterParamsTargetViewInfo = QVisualizationOuterParamsTargetViewInfo.visualizationOuterParamsTargetViewInfo;

        List<VisualizationOuterParamsInfoDTO> result = queryFactory.select(Projections.fields(VisualizationOuterParamsInfoDTO.class,
                        qVisualizationOuterParamsInfo.paramName.as("paramName"),
                        qVisualizationOuterParamsInfo.required,
                        qVisualizationOuterParamsInfo.defaultValue,
                        qVisualizationOuterParamsInfo.enabledDefault,
                        visualizationOuterParamsTargetViewInfo.targetViewId.concat("#").concat(visualizationOuterParamsTargetViewInfo.targetViewId).as("targetInfo"))).from(qVisualizationOuterParams)
                .leftJoin(qVisualizationOuterParamsInfo).on(qVisualizationOuterParamsInfo.paramsId.eq(qVisualizationOuterParams.paramsId))
                .leftJoin(visualizationOuterParamsTargetViewInfo).on(visualizationOuterParamsTargetViewInfo.paramsInfoId.eq(qVisualizationOuterParamsInfo.paramsInfoId))
                .fetch();
        return new VisualizationOuterParamsBaseResponse(Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationOuterParamsInfoDTO::getSourceInfo, VisualizationOuterParamsInfoDTO::getTargetInfoList)),
                Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationOuterParamsInfoDTO::getSourceInfo, paramsInfo -> paramsInfo))
        );
    }

    @Override
    public List<CoreDatasetGroupVO> queryDsWithVisualizationId(String visualizationId) {

        QCoreDatasetGroup qCoreDatasetGroup = QCoreDatasetGroup.coreDatasetGroup;
        QSnapshotCoreChartView qSnapshotCoreChartView = QSnapshotCoreChartView.snapshotCoreChartView;
        QSnapshotDataVisualizationInfo qSnapshotDataVisualizationInfo = QSnapshotDataVisualizationInfo.snapshotDataVisualizationInfo;
        List<CoreDatasetGroupVO> result = queryFactory.select(Projections.fields(CoreDatasetGroupVO.class,
                        qCoreDatasetGroup.id,
                        qCoreDatasetGroup.name,
                        qCoreDatasetGroup.pid,
                        qCoreDatasetGroup.level,
                        qCoreDatasetGroup.nodeType,
                        qCoreDatasetGroup.type,
                        qCoreDatasetGroup.mode,
                        qCoreDatasetGroup.info,
                        qCoreDatasetGroup.createBy,
                        qCoreDatasetGroup.createTime,
                        qCoreDatasetGroup.qrtzInstance,
                        qCoreDatasetGroup.syncStatus,
                        qCoreDatasetGroup.updateBy,
                        qCoreDatasetGroup.lastUpdateTime
                )).from(qCoreDatasetGroup)
                .innerJoin(qSnapshotCoreChartView).on(qCoreDatasetGroup.id.eq(qSnapshotCoreChartView.tableId).and(qSnapshotCoreChartView.type.ne("VQuery")))
                .innerJoin(qSnapshotDataVisualizationInfo).on(qSnapshotCoreChartView.sceneId.eq(Long.valueOf(qSnapshotDataVisualizationInfo.id.toString())))
                .where(qSnapshotCoreChartView.sceneId.eq(Long.valueOf(visualizationId)).and(qSnapshotDataVisualizationInfo.id.eq(visualizationId)))
                .where(qSnapshotDataVisualizationInfo.componentData.like("%" + qSnapshotCoreChartView.id + "%"))
                .fetch();
        if (!CollectionUtils.isEmpty(result)) {
            List<Long> activeViewIds = dataVisualizationServer.getEnabledViewIds(Long.valueOf(visualizationId), CommonConstants.RESOURCE_TABLE.SNAPSHOT);
            result.forEach(coreDatasetGroupVO -> {
                // 过滤已删除的图表
                if (!CollectionUtils.isEmpty(coreDatasetGroupVO.getDatasetViews())) {
                    coreDatasetGroupVO.setDatasetViews(coreDatasetGroupVO.getDatasetViews().stream().filter(item -> activeViewIds.contains(item.getChartId())).toList());
                }
                List<CoreDatasetTableFieldVO> fields = coreDatasetGroupVO.getDatasetFields();
                List<CoreDatasetTable> tableResult = coreDatasetTableRepository.findByDatasetGroupId(coreDatasetGroupVO.getId());
                if (!CollectionUtils.isEmpty(tableResult)) {
                    tableResult.forEach(coreDatasetTable -> {
                        String sqlVarDetail = coreDatasetTable.getSqlVariableDetails();
                        if (StringUtils.isNotEmpty(sqlVarDetail)) {
                            TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
                            };
                            List<SqlVariableDetails> defaultsSqlVariableDetails = JsonUtil.parseList(sqlVarDetail, listTypeReference);
                            defaultsSqlVariableDetails.forEach(sqlVariableDetails -> {
                                String varFieldId = coreDatasetTable.getId() + "|DE|" + sqlVariableDetails.getVariableName();
                                fields.add(new CoreDatasetTableFieldVO(varFieldId, sqlVariableDetails.getVariableName(), DeTypeConstants.DE_STRING));
                            });
                        }
                    });
                }
            });
        }
        return result;
    }
}
