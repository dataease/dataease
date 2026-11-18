package io.dataease.visualization.server;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.spreadsheet.SpreadsheetLinkJumpService;
import io.dataease.api.visualization.VisualizationLinkJumpApi;
import io.dataease.api.visualization.dto.VisualizationComponentDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.response.VisualizationLinkJumpBaseResponse;
import io.dataease.api.visualization.vo.VisualizationLinkJumpTargetViewInfoVO;
import io.dataease.api.visualization.vo.VisualizationOutParamsJumpVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.dao.auto.entity.QCoreChartView;
import io.dataease.dao.auto.entity.QCoreDatasetTableField;
import io.dataease.dao.auto.entity.QDataVisualizationInfo;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.LocalModelUtils;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.*;
import io.dataease.visualization.manage.VisualizationLinkJumpManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
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
    @Resource
    private VisualizationLinkJumpManage visualizationLinkJumpManage;

    @Resource
    private ObjectProvider<SpreadsheetLinkJumpService> spreadsheetLinkJumpServices;

    @Resource
    private ObjectProvider<InteractiveAuthApi> interactiveAuthApis;

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
            resultLinkJumpList = visualizationLinkJumpManage.queryWithDvIdSnapshot(dvId, V3UserUtil.getUid(), LocalModelUtils.isDesktop());
        } else {
            resultLinkJumpList = visualizationLinkJumpManage.queryWithDvId(dvId, V3UserUtil.getUid(), LocalModelUtils.isDesktop());
        }
        Optional.ofNullable(resultLinkJumpList).orElse(new ArrayList<>()).forEach(resultLinkJump -> {
            if (resultLinkJump.getChecked()) {
                Long sourceViewId = resultLinkJump.getSourceViewId();
                Optional.ofNullable(resultLinkJump.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
                    if (linkJumpInfo.getChecked()) {
                        String sourceJumpInfo = sourceViewId + "#" + linkJumpInfo.getSourceFieldId();
                        // 内部资源跳转必须存在目标 ID，电子表格保留独立的关联类型。
                        if ("inner".equals(linkJumpInfo.getLinkType())
                                || "spreadsheet".equals(linkJumpInfo.getLinkType())) {
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
        return visualizationLinkJumpManage.queryWithViewId(dvId, viewId, V3UserUtil.getUid(), LocalModelUtils.isDesktop());
    }

    @Transactional
    @Override
    public void updateJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        Long dvId = jumpDTO.getSourceDvId();
        Long viewId = jumpDTO.getSourceViewId();
        Assert.notNull(dvId, "dvId cannot be null");
        Assert.notNull(viewId, "viewId cannot be null");
        // 先校验所有生效的电子表格目标，再替换快照，避免失效映射落库或删除旧配置。
        Map<Long, List<VisualizationLinkJumpTargetViewInfoVO>> spreadsheetTargets = new LinkedHashMap<>();
        for (VisualizationLinkJumpInfoDTO info : Optional.ofNullable(jumpDTO.getLinkJumpInfoArray()).orElse(List.of())) {
            if (info == null) {
                throw invalidJump();
            }
            // 字段配置可以先于图表跳转开关保存，勾选的电子表格目标都必须校验。
            if ("spreadsheet".equals(info.getLinkType()) && Boolean.TRUE.equals(info.getChecked())) {
                if (info.getTargetDvId() == null) {
                    throw invalidJump();
                }
                for (var target : Optional.ofNullable(info.getTargetViewInfoList()).orElse(List.of())) {
                    if (target == null || target.getSourceFieldActiveId() == null) {
                        throw invalidJump();
                    }
                }
                spreadsheetTargets.computeIfAbsent(info.getTargetDvId(), key -> new ArrayList<>())
                        .addAll(Optional.ofNullable(info.getTargetViewInfoList()).orElse(List.of()));
            }
        }
        if (!spreadsheetTargets.isEmpty()) {
            authorizeSpreadsheetJumpSource(dvId, AuthEnum.MANAGE);
            QSnapshotCoreChartView sourceView = QSnapshotCoreChartView.snapshotCoreChartView;
            if (queryFactory.selectOne().from(sourceView)
                    .where(sourceView.id.eq(viewId), sourceView.sceneId.eq(dvId)).fetchFirst() == null) {
                throw invalidJump();
            }
            SpreadsheetLinkJumpService service = requireSpreadsheetLinkJumpService();
            spreadsheetTargets.forEach(service::validateTargets);
        }
        //清理原有数据
        visualizationLinkJumpManage.deleteJumpTargetViewInfoSnapshot(dvId, viewId);
        visualizationLinkJumpManage.deleteJumpInfoSnapshot(dvId, viewId);
        visualizationLinkJumpManage.deleteJumpSnapshot(dvId, viewId);

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
        boolean spreadsheet = "spreadsheet".equals(request.getTargetResourceType());
        if (spreadsheet) {
            if (request.getSourceViewId() == null || request.getSourceFieldId() == null || request.getTargetDvId() == null
                    || request.getResourceTable() == null
                    || !Set.of(CommonConstants.RESOURCE_TABLE.CORE, CommonConstants.RESOURCE_TABLE.SNAPSHOT)
                    .contains(request.getResourceTable())) {
                throw invalidJump();
            }
            // 跳转信息只读取映射，数据集权限和过滤字段有效性由各实例取数时校验。
            InteractiveAuthApi auth = interactiveAuthApis.getIfAvailable();
            if (!LicenseUtil.licenseValid() || auth == null) {
                throw DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
            }
            authorizeSpreadsheetJumpSource(request.getSourceDvId(),
                    CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable()) ? AuthEnum.MANAGE : AuthEnum.READ);
            auth.checkAuth(new BusiPerCheckDTO(request.getTargetDvId(), BusiResourceEnum.SPREADSHEET, AuthEnum.READ));
        }
        List<VisualizationLinkJumpDTO> result = null;
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable())) {
            QSnapshotVisualizationLinkJumpTargetViewInfo jtvi = QSnapshotVisualizationLinkJumpTargetViewInfo.snapshotVisualizationLinkJumpTargetViewInfo;
            QSnapshotVisualizationLinkJumpInfo lji = QSnapshotVisualizationLinkJumpInfo.snapshotVisualizationLinkJumpInfo;
            QSnapshotVisualizationLinkJump lj = QSnapshotVisualizationLinkJump.snapshotVisualizationLinkJump;

            JPAQuery<VisualizationLinkJumpDTO> jpaQuery = queryFactory.select(
                            Projections.fields(VisualizationLinkJumpDTO.class,
                                    lj.sourceViewId.stringValue().concat("#").concat(jtvi.sourceFieldActiveId.stringValue()).as("sourceInfo"),
                                    jtvi.targetViewId.stringValue().concat("#").concat(jtvi.targetFieldId.stringValue()).as("targetInfo")
                            ))
                    .from(jtvi)
                    .leftJoin(lji).on(jtvi.linkJumpInfoId.eq(lji.id))
                    .leftJoin(lj).on(lji.linkJumpId.eq(lj.id))
                    .where(lji.checked.eq(true))
                    .where(lj.sourceDvId.eq(request.getSourceDvId()))
                    .where(lj.sourceViewId.eq(request.getSourceViewId()))
                    .where(lji.targetDvId.eq(request.getTargetDvId()));

            if (request.getSourceFieldId() != null) {
                jpaQuery.where(lji.sourceFieldId.eq(request.getSourceFieldId()));
            }
            if (spreadsheet) {
                jpaQuery.where(lji.linkType.eq("spreadsheet"), jtvi.targetType.eq("view"));
            } else {
                jpaQuery.where(lji.linkType.ne("spreadsheet").or(lji.linkType.isNull()));
            }
            result = jpaQuery.fetch();


        } else {
            QVisualizationLinkJumpTargetViewInfo jtvi = QVisualizationLinkJumpTargetViewInfo.visualizationLinkJumpTargetViewInfo;
            QVisualizationLinkJumpInfo lji = QVisualizationLinkJumpInfo.visualizationLinkJumpInfo;
            QVisualizationLinkJump lj = QVisualizationLinkJump.visualizationLinkJump;

            JPAQuery<VisualizationLinkJumpDTO> jpaQuery = queryFactory.select(
                            Projections.fields(VisualizationLinkJumpDTO.class,
                                    lj.sourceViewId.stringValue().concat("#").concat(jtvi.sourceFieldActiveId.stringValue()).as("sourceInfo"),
                                    jtvi.targetViewId.stringValue().concat("#").concat(jtvi.targetFieldId.stringValue()).as("targetInfo")
                            ))
                    .from(jtvi)
                    .leftJoin(lji).on(jtvi.linkJumpInfoId.eq(lji.id))
                    .leftJoin(lj).on(lji.linkJumpId.eq(lj.id))
                    .where(lji.checked.eq(true))
                    .where(lj.sourceDvId.eq(request.getSourceDvId()))
                    .where(lj.sourceViewId.eq(request.getSourceViewId()))
                    .where(lji.targetDvId.eq(request.getTargetDvId()));

            if (request.getSourceFieldId() != null) {
                jpaQuery.where(lji.sourceFieldId.eq(request.getSourceFieldId()));
            }
            if (spreadsheet) {
                jpaQuery.where(lji.linkType.eq("spreadsheet"), jtvi.targetType.eq("view"));
            } else {
                jpaQuery.where(lji.linkType.ne("spreadsheet").or(lji.linkType.isNull()));
            }
            result = jpaQuery.fetch();

        }
        // 按sourceInfo分组聚合targetInfo
        Map<String, List<String>> groupedResults = result.stream()
                .collect(Collectors.groupingBy(
                        VisualizationLinkJumpDTO::getSourceInfo,
                        Collectors.mapping(
                                VisualizationLinkJumpDTO::getTargetInfo,
                                Collectors.toList()
                        )
                ));

        // 创建新的VisualizationLinkJumpDTO列表，每个包含sourceInfo和对应的targetInfo列表
        List<VisualizationLinkJumpDTO> aggregatedResult = groupedResults.entrySet().stream()
                .map(entry -> {
                    VisualizationLinkJumpDTO dto = new VisualizationLinkJumpDTO();
                    dto.setSourceInfo(entry.getKey());
                    dto.setTargetInfoList(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());

        return new VisualizationLinkJumpBaseResponse(null, Optional.ofNullable(aggregatedResult).orElse(new ArrayList<>()).stream().filter(item -> StringUtils.isNotEmpty(item.getSourceInfo())).collect(Collectors.toMap(VisualizationLinkJumpDTO::getSourceInfo, VisualizationLinkJumpDTO::getTargetInfoList)));
    }

    private void authorizeSpreadsheetJumpSource(Long dvId, AuthEnum permission) {
        if (dvId == null) {
            throw invalidJump();
        }
        DataVisualizationInfo source = dataVisualizationInfoMapper.findById(dvId).orElseThrow(this::invalidJump);
        if (Boolean.TRUE.equals(source.getDeleteFlag()) || !"leaf".equals(source.getNodeType())
                || !("dashboard".equals(source.getType()) || "dataV".equals(source.getType()))) {
            throw invalidJump();
        }
        InteractiveAuthApi auth = interactiveAuthApis.getIfAvailable();
        if (auth == null) {
            throw DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
        }
        BusiResourceEnum resource = "dataV".equals(source.getType()) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL;
        auth.checkAuth(new BusiPerCheckDTO(dvId, resource, permission));
    }

    private SpreadsheetLinkJumpService requireSpreadsheetLinkJumpService() {
        SpreadsheetLinkJumpService service = spreadsheetLinkJumpServices.getIfAvailable();
        if (service == null) {
            throw DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
        }
        return service;
    }

    private DEException invalidJump() {
        return DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
    }

    @Override
    public VisualizationComponentDTO viewTableDetailList(Long dvId) {
        DataVisualizationInfo dvInfo = dataVisualizationInfoMapper.findById(dvId).orElse(null);
        List<VisualizationViewTableVO> result;
        List<VisualizationOutParamsJumpVO> outParamsJumpInfos;
        String componentData;
        if (dvInfo != null) {
            outParamsJumpInfos = new ArrayList<>();
            QCoreChartView coreChartView = QCoreChartView.coreChartView;
            QCoreDatasetTableField coreDatasetTableField = QCoreDatasetTableField.coreDatasetTableField;
            QDataVisualizationInfo dataVisualizationInfo = QDataVisualizationInfo.dataVisualizationInfo;
            List<Tuple> tuples = queryFactory
                    .select(
                            coreChartView.id,
                            coreChartView.title,
                            coreChartView.type,
                            coreChartView.sceneId,
                            coreDatasetTableField.id,
                            coreDatasetTableField.originName,
                            coreDatasetTableField.name,
                            coreDatasetTableField.type,
                            coreDatasetTableField.deType
                    )
                    .from(coreChartView)
                    .leftJoin(coreDatasetTableField).on(coreChartView.tableId.eq(coreDatasetTableField.datasetGroupId))
                    .innerJoin(dataVisualizationInfo).on(coreChartView.sceneId.eq(dataVisualizationInfo.id))
                    .where(coreChartView.sceneId.eq(dvId))
                    .where(coreChartView.type.ne("VQuery"))
                    .where(coreChartView.tableId.isNotNull())
                    .where(dataVisualizationInfo.id.eq(dvId))
                    .fetch();
            Map<Long, VisualizationViewTableVO> resultMap = new LinkedHashMap<>();


            for (Tuple tuple : tuples) {
                Long viewId = tuple.get(coreChartView.id);
                VisualizationViewTableVO vo = resultMap.computeIfAbsent(viewId, k -> {
                    VisualizationViewTableVO newVo = new VisualizationViewTableVO();
                    newVo.setId(viewId);
                    newVo.setTitle(tuple.get(coreChartView.title));
                    newVo.setType(tuple.get(coreChartView.type));
                    newVo.setDvId(tuple.get(coreChartView.sceneId));
                    newVo.setTableFields(new ArrayList<>());
                    return newVo;
                });

                // 添加字段信息
                if (tuple.get(coreDatasetTableField.id) != null) {
                    DatasetTableFieldDTO fieldDto = new DatasetTableFieldDTO();
                    fieldDto.setId(tuple.get(coreDatasetTableField.id));
                    fieldDto.setOriginName(tuple.get(coreDatasetTableField.originName));
                    fieldDto.setName(tuple.get(coreDatasetTableField.name));
                    fieldDto.setType(tuple.get(coreDatasetTableField.type));
                    fieldDto.setDeType(tuple.get(coreDatasetTableField.deType));
                    vo.getTableFields().add(fieldDto);
                }
            }
            result = new ArrayList<>(resultMap.values());
            componentData = dvInfo.getComponentData();

            QVisualizationOuterParamsInfo visualizationOuterParamsInfo = QVisualizationOuterParamsInfo.visualizationOuterParamsInfo;
            QVisualizationOuterParams visualizationOuterParams = QVisualizationOuterParams.visualizationOuterParams;

            queryFactory.select(Projections.fields(VisualizationOutParamsJumpVO.class,
                            visualizationOuterParamsInfo.paramsInfoId.as("id"),
                            visualizationOuterParamsInfo.paramName.as("name"),
                            visualizationOuterParamsInfo.paramName.as("title")
                    )).from(visualizationOuterParamsInfo)
                    .leftJoin(visualizationOuterParams).on(visualizationOuterParamsInfo.paramsId.eq(visualizationOuterParams.paramsId))
                    .where(visualizationOuterParams.visualizationId.eq(String.valueOf(dvId))).fetch().forEach(outParamsJumpVO -> {
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
        snapshotCoreChartViewRepository.updateJumpActiveById(request.getSourceViewId(), request.getActiveStatus());
        if (request.getSourceDvId() != null) {
            return queryVisualizationJumpInfo(request.getSourceDvId(), CommonConstants.RESOURCE_TABLE.SNAPSHOT);
        } else {
            return new VisualizationLinkJumpBaseResponse();
        }
    }

    @Override
    public void removeJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        //清理原有数据
        visualizationLinkJumpManage.deleteJumpTargetViewInfoSnapshot(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
        visualizationLinkJumpManage.deleteJumpInfoSnapshot(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
        visualizationLinkJumpManage.deleteJumpSnapshot(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
    }

}
