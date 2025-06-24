package io.dataease.visualization.server;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.visualization.VisualizationLinkJumpApi;
import io.dataease.api.visualization.dto.VisualizationComponentDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.response.VisualizationLinkJumpBaseResponse;
import io.dataease.api.visualization.vo.VisualizationOutParamsJumpVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.dao.auto.entity.QCoreDatasetTableField;
import io.dataease.dao.auto.entity.QDataVisualizationInfo;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.ModelUtils;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.*;
import io.dataease.visualization.dao.ext.mapper.ExtVisualizationLinkJumpMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:31
 */
@RestController
@RequestMapping("linkJump")
public class VisualizationLinkJumpService implements VisualizationLinkJumpApi {
    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private ExtVisualizationLinkJumpMapper extVisualizationLinkJumpMapper;

    @Resource
    private SnapshotVisualizationLinkJumpRepository snapshotVisualizationLinkJumpRepository;

    @Resource
    private SnapshotVisualizationLinkJumpInfoRepository snapshotVisualizationLinkJumpInfoRepository;

    @Resource
    private SnapshotVisualizationLinkJumpTargetViewInfoRepository snapshotVisualizationLinkJumpTargetViewInfoRepository;

    @Resource
    private SnapshotCoreChartViewRepository snapshotCoreChartViewRepository;

    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoMapper;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

    @Override
    public List<DatasetTableFieldDTO> getTableFieldWithViewId(Long viewId) {
        return datasetTableFieldManage.queryTableFieldWithViewId(viewId);
    }

    @DeLinkPermit
    //获取仪表板的跳转信息
    @Override
    public VisualizationLinkJumpBaseResponse queryVisualizationJumpInfo(Long dvId, String resourceTable) {
        Map<String, VisualizationLinkJumpInfoDTO> resultBase = new HashMap<>();
        List<VisualizationLinkJumpDTO> resultLinkJumpList = null;
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(resourceTable)) {
            resultLinkJumpList = extVisualizationLinkJumpMapper.queryWithDvIdSnapshot(dvId, AuthUtils.getUser().getUserId(), ModelUtils.isDesktop());
        } else {
            resultLinkJumpList = extVisualizationLinkJumpMapper.queryWithDvId(dvId, AuthUtils.getUser().getUserId(), ModelUtils.isDesktop());
        }
        Optional.ofNullable(resultLinkJumpList).orElse(new ArrayList<>()).forEach(resultLinkJump -> {
            if (resultLinkJump.getChecked()) {
                Long sourceViewId = resultLinkJump.getSourceViewId();
                Optional.ofNullable(resultLinkJump.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
                    if (linkJumpInfo.getChecked()) {
                        String sourceJumpInfo = sourceViewId + "#" + linkJumpInfo.getSourceFieldId();
                        // 内部仪表板跳转 需要设置好仪表板ID
                        if ("inner".equals(linkJumpInfo.getLinkType())) {
                            if (linkJumpInfo.getTargetDvId() != null) {
                                resultBase.put(sourceJumpInfo, linkJumpInfo);
                            }
                        } else {
                            // 外部跳转
                            resultBase.put(sourceJumpInfo, linkJumpInfo);
                        }
                    }
                });
            }
        });
        return new VisualizationLinkJumpBaseResponse(resultBase, null);
    }

    @Override
    public VisualizationLinkJumpDTO queryWithViewId(Long dvId, Long viewId) {
        return extVisualizationLinkJumpMapper.queryWithViewId(dvId, viewId, AuthUtils.getUser().getUserId(), ModelUtils.isDesktop());
    }

    @Transactional
    @Override
    public void updateJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        Long dvId = jumpDTO.getSourceDvId();
        Long viewId = jumpDTO.getSourceViewId();
        Assert.notNull(dvId, "dvId cannot be null");
        Assert.notNull(viewId, "viewId cannot be null");
        //清理原有数据
        extVisualizationLinkJumpMapper.deleteJumpTargetViewInfoSnapshot(dvId, viewId);
        extVisualizationLinkJumpMapper.deleteJumpInfoSnapshot(dvId, viewId);
        extVisualizationLinkJumpMapper.deleteJumpSnapshot(dvId, viewId);

        // 插入新的数据
        Long linkJumpId = IDUtils.snowID();
        jumpDTO.setId(linkJumpId);
        SnapshotVisualizationLinkJump insertParam = new SnapshotVisualizationLinkJump();
        BeanUtils.copyBean(insertParam, jumpDTO);
        snapshotVisualizationLinkJumpRepository.saveAndFlush(insertParam);
        Optional.ofNullable(jumpDTO.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
            Long linkJumpInfoId = IDUtils.snowID();
            linkJumpInfo.setId(linkJumpInfoId);
            linkJumpInfo.setLinkJumpId(linkJumpId);
            SnapshotVisualizationLinkJumpInfo insertJumpInfoParam = new SnapshotVisualizationLinkJumpInfo();
            BeanUtils.copyBean(insertJumpInfoParam, linkJumpInfo);
            snapshotVisualizationLinkJumpInfoRepository.saveAndFlush(insertJumpInfoParam);
            Optional.ofNullable(linkJumpInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                Long targetViewInfoId = IDUtils.snowID();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setLinkJumpInfoId(linkJumpInfoId);
                SnapshotVisualizationLinkJumpTargetViewInfo insertTargetViewInfoParam = new SnapshotVisualizationLinkJumpTargetViewInfo();
                BeanUtils.copyBean(insertTargetViewInfoParam, targetViewInfo);
                snapshotVisualizationLinkJumpTargetViewInfoRepository.saveAndFlush(insertTargetViewInfoParam);
            });
        });
    }

    @DeLinkPermit("#p0.targetDvId")
    @Override
    public VisualizationLinkJumpBaseResponse queryTargetVisualizationJumpInfo(VisualizationLinkJumpBaseRequest request) {
        List<VisualizationLinkJumpDTO> result = null;
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable())) {
            QSnapshotVisualizationLinkJumpTargetViewInfo visualizationLinkJumpTargetViewInfo = QSnapshotVisualizationLinkJumpTargetViewInfo.snapshotVisualizationLinkJumpTargetViewInfo;
            QSnapshotVisualizationLinkJumpInfo linkJumpInfo = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
            QSnapshotVisualizationLinkJump visualizationLinkJump = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;

            JPAQuery<VisualizationLinkJumpDTO> jpaQuery = queryFactory.select(
                            Projections.constructor(VisualizationLinkJumpDTO.class,
                                    visualizationLinkJump.sourceViewId.stringValue().concat("#").concat(visualizationLinkJumpTargetViewInfo.sourceFieldActiveId.stringValue()).as("sourceInfo"),
                                    visualizationLinkJumpTargetViewInfo.targetViewId.stringValue().concat("#").concat(visualizationLinkJumpTargetViewInfo.targetFieldId.stringValue()).as("targetInfo")
                            ))
                    .from(visualizationLinkJumpTargetViewInfo)
                    .leftJoin(linkJumpInfo).on(visualizationLinkJumpTargetViewInfo.linkJumpInfoId.eq(linkJumpInfo.id))
                    .leftJoin(visualizationLinkJump).on(linkJumpInfo.linkJumpId.eq(visualizationLinkJump.id))
                    .where(linkJumpInfo.checked.eq(true))
                    .where(visualizationLinkJump.sourceDvId.eq(request.getSourceDvId()))
                    .where(visualizationLinkJump.sourceViewId.eq(request.getSourceViewId()))
                    .where(linkJumpInfo.targetDvId.eq(request.getTargetDvId()));

            if (request.getSourceFieldId() != null) {
                jpaQuery.where(linkJumpInfo.sourceFieldId.eq(request.getSourceFieldId()));
            }
            result = jpaQuery.fetch();


        } else {
            QVisualizationLinkJumpTargetViewInfo visualizationLinkJumpTargetViewInfo = QVisualizationLinkJumpTargetViewInfo.visualizationLinkJumpTargetViewInfo;
            QVisualizationLinkJumpInfo linkJumpInfo = QVisualizationLinkJumpInfo.visualizationLinkJumpInfo;
            QVisualizationLinkJump visualizationLinkJump = QVisualizationLinkJump.visualizationLinkJump;

            JPAQuery<VisualizationLinkJumpDTO> jpaQuery = queryFactory.select(
                            Projections.constructor(VisualizationLinkJumpDTO.class,
                                    visualizationLinkJump.sourceViewId.stringValue().concat("#").concat(visualizationLinkJumpTargetViewInfo.sourceFieldActiveId.stringValue()).as("sourceInfo"),
                                    visualizationLinkJumpTargetViewInfo.targetViewId.stringValue().concat("#").concat(visualizationLinkJumpTargetViewInfo.targetFieldId.stringValue()).as("targetInfo")
                            ))
                    .from(visualizationLinkJumpTargetViewInfo)
                    .leftJoin(linkJumpInfo).on(visualizationLinkJumpTargetViewInfo.linkJumpInfoId.eq(linkJumpInfo.id))
                    .leftJoin(visualizationLinkJump).on(linkJumpInfo.linkJumpId.eq(visualizationLinkJump.id))
                    .where(linkJumpInfo.checked.eq(true))
                    .where(visualizationLinkJump.sourceDvId.eq(request.getSourceDvId()))
                    .where(visualizationLinkJump.sourceViewId.eq(request.getSourceViewId()))
                    .where(linkJumpInfo.targetDvId.eq(request.getTargetDvId()));

            if (request.getSourceFieldId() != null) {
                jpaQuery.where(linkJumpInfo.sourceFieldId.eq(request.getSourceFieldId()));
            }
            result = jpaQuery.fetch();

        }
        return new VisualizationLinkJumpBaseResponse(null, Optional.ofNullable(result).orElse(new ArrayList<>()).stream().filter(item -> StringUtils.isNotEmpty(item.getSourceInfo())).collect(Collectors.toMap(VisualizationLinkJumpDTO::getSourceInfo, VisualizationLinkJumpDTO::getTargetInfoList)));
    }

    @Override
    public VisualizationComponentDTO viewTableDetailList(Long dvId) {
        DataVisualizationInfo dvInfo = dataVisualizationInfoMapper.findById(String.valueOf(dvId)).orElse(null);
        List<VisualizationViewTableVO> result;
        List<VisualizationOutParamsJumpVO> outParamsJumpInfos;
        String componentData;
        if (dvInfo != null) {
            outParamsJumpInfos = new ArrayList<>();
            QCoreChartView coreChartView = QCoreChartView.coreChartView;
            QCoreDatasetTableField coreDatasetTableField = QCoreDatasetTableField.coreDatasetTableField;
            QDataVisualizationInfo dataVisualizationInfo = QDataVisualizationInfo.dataVisualizationInfo;
            result = queryFactory.select(Projections.constructor(VisualizationViewTableVO.class,
                            coreChartView.id.as("id"),
                            coreChartView.title.as("title"),
                            coreChartView.type.as("type"),
                            coreChartView.sceneId.as("dvId"),
                            coreDatasetTableField.id.as("fieldId"),
                            coreDatasetTableField.originName.as("originName"),
                            coreDatasetTableField.name.as("fieldName"),
                            coreDatasetTableField.type.as("fieldType"),
                            coreDatasetTableField.deType.as("deType")
                    )).from(coreChartView)
                    .leftJoin(coreDatasetTableField).on(coreChartView.tableId.eq(coreDatasetTableField.datasetGroupId))
                    .innerJoin(dataVisualizationInfo).on(coreChartView.sceneId.eq(Long.valueOf(dataVisualizationInfo.id.toString())))
                    .where(coreChartView.sceneId.eq(dvId))
                    .where(coreChartView.type.ne("VQuery"))
                    .where(coreChartView.tableId.isNotNull())
                    .where(dataVisualizationInfo.id.eq(dvId.toString()))
                    .where(dataVisualizationInfo.componentData.contains(coreChartView.id.toString())).fetch();

            componentData = dvInfo.getComponentData();

            QVisualizationOuterParamsInfo visualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
            QVisualizationOuterParams visualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;

            queryFactory.select(Projections.constructor(VisualizationOutParamsJumpVO.class,
                            visualizationOuterParamsInfo.paramsInfoId.as("id"),
                            visualizationOuterParamsInfo.paramName.as("name"),
                            visualizationOuterParamsInfo.paramName.as("title")
                    )).from(visualizationOuterParamsInfo)
                    .leftJoin(visualizationOuterParams).on(visualizationOuterParamsInfo.paramsId.eq(visualizationOuterParams.paramsId))
                    .where(visualizationOuterParams.visualizationId.eq(dvId.toString())).fetch().forEach(outParamsJumpVO -> {
                        outParamsJumpVO.setType("outerParams");
                        outParamsJumpInfos.add(outParamsJumpVO);
                    });


        } else {
            result = new ArrayList<>();
            outParamsJumpInfos = new ArrayList<>();
            componentData = "[]";
        }
        return new VisualizationComponentDTO(componentData, result, outParamsJumpInfos);

    }

    @Override
    public VisualizationLinkJumpBaseResponse updateJumpSetActive(VisualizationLinkJumpBaseRequest request) {
        SnapshotCoreChartView coreChartView = new SnapshotCoreChartView();
        coreChartView.setId(Long.valueOf(request.getSourceViewId()));
        coreChartView.setJumpActive(request.getActiveStatus());
        snapshotCoreChartViewRepository.saveAndFlush(coreChartView);
        return queryVisualizationJumpInfo(request.getSourceDvId(), CommonConstants.RESOURCE_TABLE.SNAPSHOT);
    }

    @Override
    public void removeJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        //清理原有数据
        extVisualizationLinkJumpMapper.deleteJumpTargetViewInfoSnapshot(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
        extVisualizationLinkJumpMapper.deleteJumpInfoSnapshot(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
        extVisualizationLinkJumpMapper.deleteJumpSnapshot(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
    }

}
