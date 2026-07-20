package io.dataease.visualization.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.chart.vo.ChartBaseVO;
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
import io.dataease.dao.auto.entity.QCoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableRepository;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.SnapshotVisualizationOuterParamsInfoRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotVisualizationOuterParamsRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotVisualizationOuterParamsTargetViewInfoRepository;
import io.dataease.visualization.server.DataVisualizationServer;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class VisualizationOuterParamsManage {

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


    public VisualizationOuterParamsDTO queryWithVisualizationId(Long visualizationId) {
        QSnapshotDataVisualizationInfo qSnapshotDataVisualizationInfo = QSnapshotDataVisualizationInfo.snapshotDataVisualizationInfo;
        QSnapshotVisualizationOuterParams qSnapshotVisualizationOuterParams = QSnapshotVisualizationOuterParams.snapshotVisualizationOuterParams;
        VisualizationOuterParamsDTO visualizationOuterParamsDTO = queryFactory.select(Projections.fields(VisualizationOuterParamsDTO.class,
                        qSnapshotDataVisualizationInfo.id.as("visualizationId"),
                        qSnapshotVisualizationOuterParams.checked.as("checked")
                )).from(qSnapshotDataVisualizationInfo)
                .leftJoin(qSnapshotVisualizationOuterParams).on(qSnapshotVisualizationOuterParams.visualizationId.eq(String.valueOf(visualizationId)))
                .where(qSnapshotDataVisualizationInfo.id.eq(visualizationId)).fetchFirst();
        if (visualizationOuterParamsDTO != null && visualizationOuterParamsDTO.getChecked() == null) {
            visualizationOuterParamsDTO.setChecked(false);
        }
        return visualizationOuterParamsDTO;
    }

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
        String paramsId = IDUtils.snowID().toString();
        outerParamsDTO.setParamsId(paramsId);
        SnapshotVisualizationOuterParams newOuterParams = new SnapshotVisualizationOuterParams();
        BeanUtils.copyBean(newOuterParams, outerParamsDTO);
        snapshotVisualizationOuterParamsRepository.saveAndFlush(newOuterParams);
        Map<String, String> finalParamsInfoNameIdMap = paramsInfoNameIdMap;
        Optional.ofNullable(outerParamsDTO.getOuterParamsInfoArray()).orElse(new ArrayList<>()).forEach(outerParamsInfo -> {
            String paramsInfoId = finalParamsInfoNameIdMap.get(outerParamsInfo.getParamName());
            if (paramsInfoId == null) {
                paramsInfoId = IDUtils.snowID().toString();
            }
            outerParamsInfo.setParamsInfoId(paramsInfoId);
            outerParamsInfo.setParamsId(paramsId);
            SnapshotVisualizationOuterParamsInfo newOuterParamsInfo = new SnapshotVisualizationOuterParamsInfo();
            BeanUtils.copyBean(newOuterParamsInfo, outerParamsInfo);
            snapshotVisualizationOuterParamsInfoRepository.saveAndFlush(newOuterParamsInfo);
            String finalParamsInfoId = paramsInfoId;
            Optional.ofNullable(outerParamsInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = IDUtils.snowID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setParamsInfoId(finalParamsInfoId);
                SnapshotVisualizationOuterParamsTargetViewInfo newOuterParamsTargetViewInfo = new SnapshotVisualizationOuterParamsTargetViewInfo();
                BeanUtils.copyBean(newOuterParamsTargetViewInfo, targetViewInfo);
                snapshotVisualizationOuterParamsTargetViewInfoRepository.saveAndFlush(newOuterParamsTargetViewInfo);
            });
        });

    }

    @DeLinkPermit
    public VisualizationOuterParamsBaseResponse getOuterParamsInfo(Long visualizationId) {

        QVisualizationOuterParams qVisualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;
        QVisualizationOuterParamsInfo qVisualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
        QVisualizationOuterParamsTargetViewInfo visualizationOuterParamsTargetViewInfo = QVisualizationOuterParamsTargetViewInfo.visualizationOuterParamsTargetViewInfo;

        List<VisualizationOuterParamsInfoDTO> result = queryFactory.select(Projections.fields(VisualizationOuterParamsInfoDTO.class,
                        qVisualizationOuterParamsInfo.paramName.as("paramName"),
                        qVisualizationOuterParamsInfo.required,
                        qVisualizationOuterParamsInfo.defaultValue,
                        qVisualizationOuterParamsInfo.enabledDefault,
                        visualizationOuterParamsTargetViewInfo.targetViewId.stringValue().concat("#").concat(visualizationOuterParamsTargetViewInfo.targetViewId.stringValue()).as("targetInfo"))).from(qVisualizationOuterParams)
                .leftJoin(qVisualizationOuterParamsInfo).on(qVisualizationOuterParamsInfo.paramsId.eq(qVisualizationOuterParams.paramsId))
                .leftJoin(visualizationOuterParamsTargetViewInfo).on(visualizationOuterParamsTargetViewInfo.paramsInfoId.eq(qVisualizationOuterParamsInfo.paramsInfoId))
                .fetch();
        return new VisualizationOuterParamsBaseResponse(Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationOuterParamsInfoDTO::getSourceInfo, VisualizationOuterParamsInfoDTO::getTargetInfoList)),
                Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationOuterParamsInfoDTO::getSourceInfo, paramsInfo -> paramsInfo))
        );
    }

    private List<CoreDatasetTableFieldVO> getDsFieldInfo(Long datasetGroupId) {
        QCoreDatasetTableField qField = QCoreDatasetTableField.coreDatasetTableField;
        return queryFactory.select(Projections.fields(CoreDatasetTableFieldVO.class,
                        qField.id,
                        qField.datasourceId,
                        qField.datasetTableId,
                        qField.datasetGroupId,
                        qField.chartId,
                        qField.originName,
                        qField.name,
                        qField.description,
                        qField.dataeaseName,
                        qField.fieldShortName,
                        qField.groupList,
                        qField.otherGroup,
                        qField.groupType,
                        qField.type,
                        qField.size,
                        qField.deType,
                        qField.deExtractType,
                        qField.type,
                        qField.extField,
                        qField.checked,
                        qField.columnIndex,
                        qField.lastSyncTime,
                        qField.accuracy,
                        qField.dateFormat,
                        qField.dateFormatType,
                        qField.params,
                        qField.id.stringValue().as("attachId") // 别名映射
                ))
                .from(qField)
                .where(qField.datasetGroupId.eq(datasetGroupId))
                .orderBy(qField.deType.asc(), qField.originName.asc())
                .fetch();
    }

    public List<ChartBaseVO> getViewInfo(Long datasetGroupId, Long visualizationId) {
        QSnapshotCoreChartView ccv = QSnapshotCoreChartView.snapshotCoreChartView;
        QSnapshotDataVisualizationInfo dvi = QSnapshotDataVisualizationInfo.snapshotDataVisualizationInfo;

        return queryFactory.select(Projections.fields(ChartBaseVO.class,
                        ccv.id.as("chartId"),
                        ccv.title.as("chartName"),
                        ccv.type.as("chartType")
                ))
                .from(ccv)
                .innerJoin(dvi).on(ccv.sceneId.eq(dvi.id))
                .where(ccv.tableId.eq(datasetGroupId)
                        .and(ccv.type.ne("VQuery"))
                        .and(dvi.id.eq(visualizationId))
                        .and(Expressions.booleanTemplate(
                                        "{0} like concat('%', {1}, '%')",
                                        dvi.componentData,
                                        ccv.id)))
                .distinct()
                .fetch();
    }

    public List<CoreDatasetGroupVO> queryDsWithVisualizationId(Long visualizationId) {

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
                .innerJoin(qSnapshotDataVisualizationInfo).on(qSnapshotCoreChartView.sceneId.eq(qSnapshotDataVisualizationInfo.id))
                .where(qSnapshotCoreChartView.sceneId.eq(visualizationId).and(qSnapshotDataVisualizationInfo.id.eq(visualizationId)))
                .where(Expressions.booleanTemplate(
                        "{0} like concat('%', {1}, '%')",
                        qSnapshotDataVisualizationInfo.componentData,
                        qSnapshotCoreChartView.id
                )).fetch();
        if (!CollectionUtils.isEmpty(result)) {
            result.stream().forEach(item ->{
                item.setDatasetViews(getViewInfo(item.getId(), visualizationId));
                item.setDatasetFields(getDsFieldInfo(item.getId()));
            });
            List<Long> activeViewIds = dataVisualizationServer.getEnabledViewIds(visualizationId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
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
                                fields.add(new CoreDatasetTableFieldVO(varFieldId, sqlVariableDetails.getVariableName(), FieldUtils.transType2DeType(sqlVariableDetails.getType().get(0).contains("DATETIME") ? "DATETIME" : sqlVariableDetails.getType().get(0))));
                            });
                        }
                    });
                }
            });
        }
        return result;
    }
}
