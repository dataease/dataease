package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.template.dto.TemplateManageFileDTO;
import io.dataease.api.template.dto.VisualizationTemplateExtendDataDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.dto.VisualizationViewTableDTO;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationAppExportRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.*;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.constants.OptConstants;
import io.dataease.constant.CommonConstants;
import io.dataease.constant.LogOT;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.license.config.XpackInteract;
import io.dataease.log.DeLog;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.template.dao.auto.entity.VisualizationTemplate;
import io.dataease.template.dao.auto.entity.VisualizationTemplateExtendData;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateExtendDataMapper;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateMapper;
import io.dataease.template.dao.ext.ExtVisualizationTemplateMapper;
import io.dataease.template.manage.TemplateCenterManage;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.dataease.visualization.manage.CoreBusiManage;
import io.dataease.visualization.manage.CoreVisualizationManage;
import io.dataease.visualization.utils.VisualizationUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/dataVisualization")
public class DataVisualizationServer implements DataVisualizationApi {

    @Resource
    private DataVisualizationInfoMapper visualizationInfoMapper;

    @Resource
    private ChartViewManege chartViewManege;
    @Resource
    private CoreChartViewMapper coreChartViewMapper;

    @Resource
    private ExtDataVisualizationMapper extDataVisualizationMapper;

    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    @Resource
    private ChartDataManage chartDataManage;

    @Resource
    private VisualizationTemplateMapper templateMapper;

    @Resource
    private TemplateCenterManage templateCenterManage;

    @Resource
    private StaticResourceServer staticResourceServer;

    @Resource
    private VisualizationTemplateExtendDataMapper templateExtendDataMapper;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private VisualizationWatermarkMapper watermarkMapper;

    @Resource
    private DatasetGroupManage datasetGroupManage;

    @Resource
    private DatasetDataManage datasetDataManage;

    @Resource
    private ExtVisualizationTemplateMapper appTemplateMapper;

    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;

    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;

    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;
    @Autowired
    private CoreDatasourceMapper coreDatasourceMapper;

    @Resource
    private CoreBusiManage coreBusiManage;

    @Override
    public DataVisualizationVO findCopyResource(Long dvId, String busiFlag) {
        DataVisualizationVO result = findById(new DataVisualizationBaseRequest(dvId, busiFlag));
        if (result != null && result.getPid() == -1) {
            return result;
        } else {
            return null;
        }
    }

    @DeLog(id = "#p0.id", ot = LogOT.READ, stExp = "#p0.busiFlag")
    @Override
    @XpackInteract(value = "dataVisualizationServer", original = true)
    public DataVisualizationVO findById(DataVisualizationBaseRequest request) {
        Long dvId = request.getId();
        String busiFlag = request.getBusiFlag();
        DataVisualizationVO result = extDataVisualizationMapper.findDvInfo(dvId, busiFlag);
        if (result != null) {
            //获取图表信息
            List<ChartViewDTO> chartViewDTOS = chartViewManege.listBySceneId(dvId);
            if (!CollectionUtils.isEmpty(chartViewDTOS)) {
                Map<Long, ChartViewDTO> viewInfo = chartViewDTOS.stream().collect(Collectors.toMap(ChartViewDTO::getId, chartView -> chartView));
                result.setCanvasViewInfo(viewInfo);
            }
            VisualizationWatermark watermark = watermarkMapper.selectById("system_default");
            VisualizationWatermarkVO watermarkVO = new VisualizationWatermarkVO();
            BeanUtils.copyBean(watermarkVO, watermark);
            result.setWatermarkInfo(watermarkVO);

            if (DataVisualizationConstants.QUERY_SOURCE.REPORT.equals(request.getSource()) && request.getTaskId() != null) {
                //获取定时报告过自定义过滤组件信息
                List<VisualizationReportFilterVO> filterVOS = extDataVisualizationMapper.queryReportFilter(dvId, request.getTaskId());
                if (!CollectionUtils.isEmpty(filterVOS)) {
                    Map<Long, VisualizationReportFilterVO> reportFilterInfo = filterVOS.stream().collect(Collectors.toMap(VisualizationReportFilterVO::getFilterId, filterVo -> filterVo));
                    result.setReportFilterInfo(reportFilterInfo);
                }
            }
            if (ObjectUtils.isNotEmpty(request.getShowWatermark()) && !request.getShowWatermark()) {
                VisualizationWatermarkVO watermarkInfo = result.getWatermarkInfo();
                String settingContent = null;
                if (ObjectUtils.isNotEmpty(watermarkInfo) && StringUtils.isNotBlank(settingContent = watermarkInfo.getSettingContent())) {
                    Map map = JsonUtil.parse(settingContent, Map.class);
                    map.put("enable", false);
                    settingContent = JsonUtil.toJSONString(map).toString();
                    watermarkInfo.setSettingContent(settingContent);
                    result.setWatermarkInfo(watermarkInfo);
                }
            }
            return result;
        } else {
            DEException.throwException("资源不存在或已经被删除...");
        }
        return null;
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, stExp = "#p0.type")
    @Override
    @Transactional
    public String saveCanvas(DataVisualizationBaseRequest request) throws Exception {
        boolean isAppSave = false;
        Long time = System.currentTimeMillis();
        // 如果是应用 则新进行应用校验 数据集名称和 数据源名称校验
        VisualizationExport2AppVO appData = request.getAppData();
        Map<Long, Long> dsGroupIdMap = new HashMap<>();
        List<DatasetGroupInfoDTO> newDsGroupInfo = new ArrayList<>();
        Map<Long, Long> dsTableIdMap = new HashMap<>();
        Map<Long, Long> dsTableFieldsIdMap = new HashMap<>();
        List<CoreDatasetTableField> dsTableFieldsList = new ArrayList();
        Map<Long, Long> datasourceIdMap = new HashMap<>();
        Map<Long, Map<String, String>> dsTableNamesMap = new HashMap<>();
        List<Long> newDatasourceId = new ArrayList<>();
        if (appData != null) {
            isAppSave = true;
            try {
                List<AppCoreDatasourceVO> appCoreDatasourceVO = appData.getDatasourceInfo();

                //  app 数据源 excel 表名映射
                appCoreDatasourceVO.forEach(datasourceOld -> {
                    newDatasourceId.add(datasourceOld.getSystemDatasourceId());
                    // Excel 数据表明映射
                    if (StringUtils.isNotEmpty(datasourceOld.getConfiguration())) {
                        if (datasourceOld.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
                            dsTableNamesMap.put(datasourceOld.getId(), ExcelUtils.getTableNamesMap(datasourceOld.getConfiguration()));
                        } else if (datasourceOld.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
                            dsTableNamesMap.put(datasourceOld.getId(), ApiUtils.getTableNamesMap(datasourceOld.getConfiguration()));
                        }
                    }
                });

                List<CoreDatasource> systemDatasource = coreDatasourceMapper.selectBatchIds(newDatasourceId);
                systemDatasource.forEach(datasourceNew -> {
                    // Excel 数据表明映射
                    if (StringUtils.isNotEmpty(datasourceNew.getConfiguration())) {
                        if (datasourceNew.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
                            dsTableNamesMap.put(datasourceNew.getId(), ExcelUtils.getTableNamesMap(datasourceNew.getConfiguration()));
                        } else if (datasourceNew.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
                            dsTableNamesMap.put(datasourceNew.getId(), ApiUtils.getTableNamesMap(datasourceNew.getConfiguration()));
                        }
                    }
                });
                datasourceIdMap.putAll(appData.getDatasourceInfo().stream()
                        .collect(Collectors.toMap(AppCoreDatasourceVO::getId, AppCoreDatasourceVO::getSystemDatasourceId)));
                Long datasetFolderPid = request.getDatasetFolderPid();
                String datasetFolderName = request.getDatasetFolderName();
                //新建数据集分组
                DatasetGroupInfoDTO datasetFolderNewRequest = new DatasetGroupInfoDTO();
                datasetFolderNewRequest.setName(datasetFolderName);
                datasetFolderNewRequest.setNodeType("folder");
                datasetFolderNewRequest.setPid(datasetFolderPid);
                DatasetGroupInfoDTO datasetFolderNew = datasetGroupManage.save(datasetFolderNewRequest, false);
                Long datasetFolderNewId = datasetFolderNew.getId();
                //新建数据集
                appData.getDatasetGroupsInfo().forEach(appDatasetGroup -> {
                    if ("dataset".equals(appDatasetGroup.getNodeType())) {
                        Long oldId = appDatasetGroup.getId();
                        Long newId = IDUtils.snowID();
                        DatasetGroupInfoDTO datasetNewRequest = new DatasetGroupInfoDTO();
                        BeanUtils.copyBean(datasetNewRequest, appDatasetGroup);
                        datasetNewRequest.setId(newId);
                        datasetNewRequest.setCreateBy(AuthUtils.getUser().getUserId() + "");
                        datasetNewRequest.setUpdateBy(AuthUtils.getUser().getUserId() + "");
                        datasetNewRequest.setCreateTime(time);
                        datasetNewRequest.setLastUpdateTime(time);
                        datasetNewRequest.setPid(datasetFolderNewId);
                        try {
                            newDsGroupInfo.add(datasetNewRequest);
                            dsGroupIdMap.put(oldId, newId);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                });
                // 新建数据集表
                appData.getDatasetTablesInfo().forEach(appCoreDatasetTableVO -> {
                    Long oldId = appCoreDatasetTableVO.getId();
                    Long newId = IDUtils.snowID();
                    CoreDatasetTable datasetTable = new CoreDatasetTable();
                    BeanUtils.copyBean(datasetTable, appCoreDatasetTableVO);
                    datasetTable.setDatasetGroupId(dsGroupIdMap.get(datasetTable.getDatasetGroupId()));
                    datasetTable.setId(newId);
                    datasetTable.setDatasourceId(datasourceIdMap.get(datasetTable.getDatasourceId()));
                    coreDatasetTableMapper.insert(datasetTable);
                    dsTableIdMap.put(oldId, newId);

                });
                // 新建数据字段
                appData.getDatasetTableFieldsInfo().forEach(appDsTableFields -> {
                    Long oldId = appDsTableFields.getId();
                    Long newId = IDUtils.snowID();
                    CoreDatasetTableField dsDsField = new CoreDatasetTableField();
                    BeanUtils.copyBean(dsDsField, appDsTableFields);
                    dsDsField.setDatasetGroupId(dsGroupIdMap.get(dsDsField.getDatasetGroupId()));
                    dsDsField.setDatasetTableId(dsTableIdMap.get(dsDsField.getDatasetTableId()));
                    dsDsField.setDatasourceId(datasourceIdMap.get(dsDsField.getDatasourceId()));
                    dsDsField.setId(newId);
                    dsTableFieldsList.add(dsDsField);
                    dsTableFieldsIdMap.put(oldId, newId);
                });

                // dsTableFields 中存在计算字段在OriginName中 也需要替换
                dsTableFieldsList.forEach(dsTableFields -> {
                    dsTableFieldsIdMap.forEach((key, value) -> {
                        dsTableFields.setOriginName(dsTableFields.getOriginName().replaceAll(key.toString(), value.toString()));
                    });
                    coreDatasetTableFieldMapper.insert(dsTableFields);
                });


                // 持久化数据集
                newDsGroupInfo.forEach(dsGroup -> {
                    dsTableIdMap.forEach((key, value) -> {
                        dsGroup.setInfo(dsGroup.getInfo().replaceAll(key.toString(), value.toString()));
                    });

                    dsTableFieldsIdMap.forEach((key, value) -> {
                        dsGroup.setInfo(dsGroup.getInfo().replaceAll(key.toString(), value.toString()));
                    });

                    datasourceIdMap.forEach((key, value) -> {
                        dsGroup.setInfo(dsGroup.getInfo().replaceAll(key.toString(), value.toString()));
                        //表名映射更新
                        Map<String, String> appDsTableNamesMap = dsTableNamesMap.get(key);
                        Map<String, String> systemDsTableNamesMap = dsTableNamesMap.get(value);
                        if (!CollectionUtils.isEmpty(appDsTableNamesMap) && !CollectionUtils.isEmpty(systemDsTableNamesMap)) {
                            appDsTableNamesMap.forEach((keyName, valueName) -> {
                                if (StringUtils.isNotEmpty(systemDsTableNamesMap.get(keyName))) {
                                    dsGroup.setInfo(dsGroup.getInfo().replaceAll(valueName, systemDsTableNamesMap.get(keyName)));
                                }
                            });
                        }

                    });


                    datasetGroupManage.innerSave(dsGroup);
                });

            } catch (Exception e) {
                LogUtil.error(e);
                DEException.throwException(e);
            }
            // 更换主数据内容
            AtomicReference<String> componentDataStr = new AtomicReference<>(request.getComponentData());
            dsGroupIdMap.forEach((key, value) -> {
                componentDataStr.set(componentDataStr.get().replaceAll(key.toString(), value.toString()));
            });
            dsTableIdMap.forEach((key, value) -> {
                componentDataStr.set(componentDataStr.get().replaceAll(key.toString(), value.toString()));
            });

            dsTableFieldsIdMap.forEach((key, value) -> {
                componentDataStr.set(componentDataStr.get().replaceAll(key.toString(), value.toString()));
            });

            datasourceIdMap.forEach((key, value) -> {
                componentDataStr.set(componentDataStr.get().replaceAll(key.toString(), value.toString()));
                //表名映射更新
                Map<String, String> appDsTableNamesMap = dsTableNamesMap.get(key);
                Map<String, String> systemDsTableNamesMap = dsTableNamesMap.get(value);
                if (!CollectionUtils.isEmpty(appDsTableNamesMap) && !CollectionUtils.isEmpty(systemDsTableNamesMap)) {
                    appDsTableNamesMap.forEach((keyName, valueName) -> {
                        if (StringUtils.isNotEmpty(systemDsTableNamesMap.get(keyName))) {
                            componentDataStr.set(componentDataStr.get().replaceAll(key.toString(), value.toString()));
                        }
                    });
                }

            });
            request.setComponentData(componentDataStr.get());
        }
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        visualizationInfo.setNodeType(request.getNodeType() == null ? DataVisualizationConstants.NODE_TYPE.LEAF : request.getNodeType());
        if (request.getSelfWatermarkStatus() != null && request.getSelfWatermarkStatus()) {
            visualizationInfo.setSelfWatermarkStatus(1);
        } else {
            visualizationInfo.setSelfWatermarkStatus(0);
        }
        if (DataVisualizationConstants.RESOURCE_OPT_TYPE.COPY.equals(request.getOptType())) {
            // 复制更新 新建权限插入
            visualizationInfoMapper.deleteById(request.getId());
            visualizationInfo.setNodeType(DataVisualizationConstants.NODE_TYPE.LEAF);
        }
        Long newDvId = coreVisualizationManage.innerSave(visualizationInfo);
        request.setId(newDvId);
        // 还原ID信息
        Map<Long, ChartViewDTO> canvasViews = request.getCanvasViewInfo();
        if (isAppSave) {
            Map<Long, String> canvasViewsStr = VisualizationUtils.viewTransToStr(canvasViews);
            canvasViewsStr.forEach((viewId, viewInfoStr) -> {
                AtomicReference<String> mutableViewInfoStr = new AtomicReference<>(viewInfoStr);
                datasourceIdMap.forEach((key, value) -> {
                    mutableViewInfoStr.set(mutableViewInfoStr.get().replaceAll(key.toString(), value.toString()));
                });
                dsTableIdMap.forEach((key, value) -> {
                    mutableViewInfoStr.set(mutableViewInfoStr.get().replaceAll(key.toString(), value.toString()));
                });
                dsTableFieldsIdMap.forEach((key, value) -> {
                    mutableViewInfoStr.set(mutableViewInfoStr.get().replaceAll(key.toString(), value.toString()));
                });
                dsGroupIdMap.forEach((key, value) -> {
                    mutableViewInfoStr.set(mutableViewInfoStr.get().replaceAll(key.toString(), value.toString()));
                });
                canvasViewsStr.put(viewId, mutableViewInfoStr.get());
            });
            canvasViews = VisualizationUtils.viewTransToObj(canvasViewsStr);
            canvasViews.forEach((key, viewInfo) -> {
                viewInfo.setDataFrom("dataset");
                if (viewInfo.getTableId() == null) {
                    viewInfo.setTableId(viewInfo.getSourceTableId());
                }
            });
        }
        //保存图表信息
        chartDataManage.saveChartViewFromVisualization(request.getComponentData(), newDvId, canvasViews);
        return newDvId.toString();
    }

    @Override
    public String appCanvasNameCheck(DataVisualizationBaseRequest request) throws Exception {
        Long datasetFolderPid = request.getDatasetFolderPid();
        String datasetFolderName = request.getDatasetFolderName();
        QueryWrapper<CoreDatasetGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", datasetFolderName);
        queryWrapper.eq("pid", datasetFolderPid);
        if (coreDatasetGroupMapper.exists(queryWrapper)) {
            return "repeat";
        } else {
            return "success";
        }
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, stExp = "#p0.type")
    @Override
    @Transactional
    public void updateCanvas(DataVisualizationBaseRequest request) {
        Long dvId = request.getId();
        if (dvId == null) {
            DEException.throwException("ID can not be null");
        }
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        if (request.getSelfWatermarkStatus() != null && request.getSelfWatermarkStatus()) {
            visualizationInfo.setSelfWatermarkStatus(1);
        } else {
            visualizationInfo.setSelfWatermarkStatus(0);
        }

        // 检查当前节点的pid是否一致如果不一致 需要调用move 接口(预存 可能会出现pid =-1的情况)
        if (request.getPid() != -1) {
            QueryWrapper<DataVisualizationInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pid", request.getPid());
            queryWrapper.eq("id", dvId);
            if (!visualizationInfoMapper.exists(queryWrapper)) {
                request.setMoveFromUpdate(true);
                coreVisualizationManage.move(request);
            }
        }
        coreVisualizationManage.innerEdit(visualizationInfo);

        //保存图表信息
        chartDataManage.saveChartViewFromVisualization(request.getComponentData(), dvId, request.getCanvasViewInfo());
    }

    /**
     * @Description: 更新基础信息；
     * 为什么单独接口：1.基础信息更新频繁数据且数据载量较小；2.防止出现更新过多信息的情况，造成图表的误删等操作
     */
    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, stExp = "#p0.type")
    @Override
    @Transactional
    public void updateBase(DataVisualizationBaseRequest request) {
        if (request.getId() != null) {
            coreVisualizationManage.innerEdit(BeanUtils.copyBean(new DataVisualizationInfo(), request));
        } else {
            DEException.throwException("Id can not be null");
        }
    }

    /**
     * @Description: 逻辑删除可视化信息；将delete_flag 置为0
     */
    @DeLog(id = "#p0", ot = LogOT.DELETE, stExp = "#p1")
    @Transactional
    @Override
    public void deleteLogic(Long dvId, String busiFlag) {
        coreVisualizationManage.delete(dvId);
    }


    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        return coreVisualizationManage.tree(request);
    }

    @Override
    public Map<String, List<BusiNodeVO>> interactiveTree(Map<String, BusiNodeRequest> requestMap) {
        return coreBusiManage.interactiveTree(requestMap);
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.MODIFY, stExp = "#p0.type")
    @Transactional
    @Override
    public void move(DataVisualizationBaseRequest request) {
        coreVisualizationManage.move(request);
    }

    @Override
    public List<VisualizationResourceVO> findRecent(@RequestBody VisualizationWorkbranchQueryRequest request) {
        request.setQueryFrom("recent");
        IPage<VisualizationResourceVO> result = coreVisualizationManage.query(1, 20, request);
        return result.getRecords();
    }

    /**
     * @Description: 复制仪表板
     * 复制步骤 1.复制基础可视化数据；2.复制图表数据；3.附加数据（包括联动信息，跳转信息，外部参数信息等仪表板附加信息）
     */
    @Transactional
    @Override
    public String copy(DataVisualizationBaseRequest request) {
        Long sourceDvId = request.getId(); //源仪表板ID
        Long newDvId = IDUtils.snowID(); //目标仪表板ID
        Long copyId = IDUtils.snowID() / 100; // 本次复制执行ID
        // 复制仪表板
        DataVisualizationInfo newDv = visualizationInfoMapper.selectById(sourceDvId);
        newDv.setName(request.getName());
        newDv.setId(newDvId);
        newDv.setPid(request.getPid());
        newDv.setCreateTime(System.currentTimeMillis());
        // 复制图表 chart_view
        extDataVisualizationMapper.viewCopyWithDv(sourceDvId, newDvId, copyId);
        List<CoreChartView> viewList = extDataVisualizationMapper.findViewInfoByCopyId(copyId);
        if (!CollectionUtils.isEmpty(viewList)) {
            String componentData = newDv.getComponentData();
            // componentData viewId 数据  并保存
            for (CoreChartView viewInfo : viewList) {
                componentData = componentData.replaceAll(String.valueOf(viewInfo.getCopyFrom()), String.valueOf(viewInfo.getId()));
            }
            newDv.setComponentData(componentData);
        }
        // 复制图表联动信息
        extDataVisualizationMapper.copyLinkage(copyId);
        extDataVisualizationMapper.copyLinkageField(copyId);
        // 复制图表跳转信息
        extDataVisualizationMapper.copyLinkJump(copyId);
        extDataVisualizationMapper.copyLinkJumpInfo(copyId);
        extDataVisualizationMapper.copyLinkJumpTargetInfo(copyId);
        DataVisualizationInfo visualizationInfoTarget = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfoTarget, newDv);
        visualizationInfoTarget.setPid(-1L);
        coreVisualizationManage.preInnerSave(visualizationInfoTarget);
        return String.valueOf(newDvId);
    }

    @Override
    public String findDvType(Long dvId) {
        return extDataVisualizationMapper.findDvType(dvId);
    }

    @Override
    public DataVisualizationVO decompression(DataVisualizationBaseRequest request) throws Exception {
        try {
            Long newDvId = IDUtils.snowID();
            String newFrom = request.getNewFrom();
            String templateStyle = null;
            String templateData = null;
            String dynamicData = null;
            String staticResource = null;
            String appDataStr = null;
            String name = null;
            String dvType = null;
            Integer version = null;
            //内部模板新建
            if (DataVisualizationConstants.NEW_PANEL_FROM.NEW_INNER_TEMPLATE.equals(newFrom)) {
                VisualizationTemplate visualizationTemplate = templateMapper.selectById(request.getTemplateId());
                templateStyle = visualizationTemplate.getTemplateStyle();
                templateData = visualizationTemplate.getTemplateData();
                dynamicData = visualizationTemplate.getDynamicData();
                name = visualizationTemplate.getName();
                dvType = visualizationTemplate.getDvType();
                version = visualizationTemplate.getVersion();
                appDataStr = visualizationTemplate.getAppData();
                // 模板市场记录
                coreOptRecentManage.saveOpt(request.getTemplateId(), OptConstants.OPT_RESOURCE_TYPE.TEMPLATE, OptConstants.OPT_TYPE.NEW);
                VisualizationTemplate visualizationTemplateUpdate = new VisualizationTemplate();
                visualizationTemplateUpdate.setId(visualizationTemplate.getId());
                visualizationTemplateUpdate.setUseCount(visualizationTemplate.getUseCount() == null ? 0 : visualizationTemplate.getUseCount() + 1);
                templateMapper.updateById(visualizationTemplateUpdate);
            } else if (DataVisualizationConstants.NEW_PANEL_FROM.NEW_OUTER_TEMPLATE.equals(newFrom)) {
                templateStyle = request.getCanvasStyleData();
                templateData = request.getComponentData();
                dynamicData = request.getDynamicData();
                staticResource = request.getStaticResource();
                name = request.getName();
                dvType = request.getType();
            } else if (DataVisualizationConstants.NEW_PANEL_FROM.NEW_MARKET_TEMPLATE.equals(newFrom)) {
                TemplateManageFileDTO templateFileInfo = templateCenterManage.getTemplateFromMarketV2(request.getResourceName());
                if (templateFileInfo == null) {
                    DEException.throwException("Can't find the template's info from market,please check");
                }
                templateStyle = templateFileInfo.getCanvasStyleData();
                templateData = templateFileInfo.getComponentData();
                dynamicData = templateFileInfo.getDynamicData();
                staticResource = templateFileInfo.getStaticResource();
                name = templateFileInfo.getName();
                dvType = templateFileInfo.getDvType();
                version = templateFileInfo.getVersion();
                appDataStr = templateFileInfo.getAppData();
                // 模板市场记录
                coreOptRecentManage.saveOpt(request.getResourceName(), OptConstants.OPT_RESOURCE_TYPE.TEMPLATE, OptConstants.OPT_TYPE.NEW);
            }
            if (StringUtils.isNotEmpty(appDataStr) && appDataStr.length() > 10) {
                try {
                    VisualizationExport2AppVO appDataFormat = JsonUtil.parseObject(appDataStr, VisualizationExport2AppVO.class);
                    String dvInfo = appDataFormat.getVisualizationInfo();
                    VisualizationBaseInfoVO baseInfoVO = JsonUtil.parseObject(dvInfo, VisualizationBaseInfoVO.class);
                    Long sourceDvId = baseInfoVO.getId();
                    appDataStr = appDataStr.replaceAll(sourceDvId.toString(), newDvId.toString());
                } catch (Exception e) {
                    LogUtil.error(e);
                    appDataStr = null;
                }
            } else {
                appDataStr = null;
            }
            // 解析动态数据
            Map<String, String> dynamicDataMap = JsonUtil.parseObject(dynamicData, Map.class);
            List<ChartViewDTO> chartViews = new ArrayList<>();
            Map<Long, ChartViewDTO> canvasViewInfo = new HashMap<>();
            Map<Long, VisualizationTemplateExtendDataDTO> extendDataInfo = new HashMap<>();
            for (Map.Entry<String, String> entry : dynamicDataMap.entrySet()) {
                String originViewId = entry.getKey();
                Object viewInfo = entry.getValue();
                try {
                    // 旧模板图表过滤器适配
                    if (viewInfo instanceof Map && ((Map) viewInfo).get("customFilter") instanceof ArrayList) {
                        ((Map) viewInfo).put("customFilter", new HashMap<>());
                    }
                } catch (Exception e) {
                    LogUtil.error("History Adaptor Error", e);
                }
                String originViewData = JsonUtil.toJSONString(entry.getValue()).toString();
                ChartViewDTO chartView = JsonUtil.parseObject(originViewData, ChartViewDTO.class);
                if (chartView == null) {
                    continue;
                }
                Long newViewId = IDUtils.snowID();
                chartView.setId(newViewId);
                chartView.setSceneId(newDvId);
                chartView.setSourceTableId(chartView.getTableId());
                chartView.setTableId(null);

                chartView.setDataFrom(CommonConstants.VIEW_DATA_FROM.TEMPLATE);
                // 数据处理 1.替换viewId 2.加入模板view data数据
                VisualizationTemplateExtendDataDTO extendDataDTO = new VisualizationTemplateExtendDataDTO(newDvId, newViewId, originViewData);
                extendDataInfo.put(newViewId, extendDataDTO);
                templateData = templateData.replaceAll(originViewId, newViewId.toString());
                if (StringUtils.isNotEmpty(appDataStr)) {
                    chartView.setTableId(chartView.getSourceTableId());
                    appDataStr = appDataStr.replaceAll(originViewId, newViewId.toString());
                }
                canvasViewInfo.put(chartView.getId(), chartView);
                //插入模板数据 此处预先插入减少数据交互量
                VisualizationTemplateExtendData extendData = new VisualizationTemplateExtendData();
                templateExtendDataMapper.insert(BeanUtils.copyBean(extendData, extendDataDTO));
            }
            request.setComponentData(templateData);
            request.setCanvasStyleData(templateStyle);
            //Store static resource into the server
            staticResourceServer.saveFilesToServe(staticResource);
            return new DataVisualizationVO(newDvId, name, dvType, version, templateStyle, templateData, appDataStr, canvasViewInfo, null);
        } catch (Exception e) {
            e.printStackTrace();
            DEException.throwException("解析错误");
            return null;
        }

    }

    @Override
    public DataVisualizationVO decompressionLocalFile(MultipartFile file) {
        return null;
    }

    @Override
    public List<VisualizationViewTableDTO> detailList(Long dvId) {
        List<VisualizationViewTableDTO> result = extDataVisualizationMapper.getVisualizationViewDetails(dvId);
        DataVisualizationInfo dvInfo = visualizationInfoMapper.selectById(dvId);
        if (dvInfo != null && !CollectionUtils.isEmpty(result)) {
            String componentData = dvInfo.getComponentData();
            return result.stream().filter(item -> componentData.indexOf(String.valueOf(item.getId())) > 0).toList();
        } else {
            return result;
        }
    }

    @Override
    public VisualizationExport2AppVO export2AppCheck(VisualizationAppExportRequest appExportRequest) {
        List<Long> viewIds = appExportRequest.getViewIds();
        List<Long> dsIds = appExportRequest.getDsIds();
        Long dvId = appExportRequest.getDvId();
        List<AppCoreChartViewVO> chartViewVOInfo = null;
        List<AppCoreDatasetGroupVO> datasetGroupVOInfo = null;
        List<AppCoreDatasetTableVO> datasetTableVOInfo = null;
        List<AppCoreDatasetTableFieldVO> datasetTableFieldVOInfo = null;
        List<AppCoreDatasourceVO> datasourceVOInfo = null;
        List<AppCoreDatasourceTaskVO> datasourceTaskVOInfo = null;
        //获取所有视图信息
        if (!CollectionUtils.isEmpty(viewIds)) {
            chartViewVOInfo = appTemplateMapper.findAppViewInfo(viewIds);
        }
        if (!CollectionUtils.isEmpty(dsIds)) {
            datasetGroupVOInfo = appTemplateMapper.findAppDatasetGroupInfo(dsIds);
            datasetTableVOInfo = appTemplateMapper.findAppDatasetTableInfo(dsIds);
            datasetTableFieldVOInfo = appTemplateMapper.findAppDatasetTableFieldInfo(dsIds);
            datasourceVOInfo = appTemplateMapper.findAppDatasourceInfo(dsIds);
            datasourceTaskVOInfo = appTemplateMapper.findAppDatasourceTaskInfo(dsIds);
        }

        if (CollectionUtils.isEmpty(datasourceVOInfo)) {
            DEException.throwException("当前不存在数据源无法导出");
        }

        List<VisualizationLinkageVO> linkageVOInfo = appTemplateMapper.findAppLinkageInfo(dvId);
        List<VisualizationLinkageFieldVO> linkageFieldVOInfo = appTemplateMapper.findAppLinkageFieldInfo(dvId);
        List<VisualizationLinkJumpVO> linkJumpVOInfo = appTemplateMapper.findAppLinkJumpInfo(dvId);
        List<VisualizationLinkJumpInfoVO> linkJumpInfoVOInfo = appTemplateMapper.findAppLinkJumpInfoInfo(dvId);
        List<VisualizationLinkJumpTargetViewInfoVO> listJumpTargetViewInfoVO = appTemplateMapper.findAppLinkJumpTargetViewInfoInfo(dvId);

        return new VisualizationExport2AppVO(chartViewVOInfo, datasetGroupVOInfo, datasetTableVOInfo,
                datasetTableFieldVOInfo, datasourceVOInfo, datasourceTaskVOInfo,
                linkJumpVOInfo, linkJumpInfoVOInfo, listJumpTargetViewInfoVO, linkageVOInfo, linkageFieldVOInfo);
    }


    @Override
    public void nameCheck(DataVisualizationBaseRequest request) {
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        if (DataVisualizationConstants.RESOURCE_OPT_TYPE.MOVE.equals(request.getOpt())
                || DataVisualizationConstants.RESOURCE_OPT_TYPE.RENAME.equals(request.getOpt())
                || DataVisualizationConstants.RESOURCE_OPT_TYPE.EDIT.equals(request.getOpt())
                || DataVisualizationConstants.RESOURCE_OPT_TYPE.COPY.equals(request.getOpt())) {
            if (request.getPid() == null) {
                DataVisualizationInfo result = visualizationInfoMapper.selectById(request.getId());
                request.setPid(result.getPid());
            }
            if (DataVisualizationConstants.RESOURCE_OPT_TYPE.MOVE.equals(request.getOpt())
                    || DataVisualizationConstants.RESOURCE_OPT_TYPE.RENAME.equals(request.getOpt())
                    || DataVisualizationConstants.RESOURCE_OPT_TYPE.EDIT.equals(request.getOpt())) {
                wrapper.ne("id", request.getId());
            }
        }
        wrapper.eq("delete_flag", 0);
        wrapper.eq("pid", request.getPid());
        wrapper.ne("pid", -1);
        wrapper.eq("name", request.getName().trim());
        wrapper.eq("node_type", request.getNodeType());
        wrapper.eq("type", request.getType());
        wrapper.eq("org_id", AuthUtils.getUser().getDefaultOid());
        if (visualizationInfoMapper.exists(wrapper)) {
            DEException.throwException("当前名称已经存在");
        }
    }

    public String getAbsPath(String id) {
        CoreChartView coreChartView = coreChartViewMapper.selectById(id);
        if (coreChartView == null) {
            return null;
        }
        if (coreChartView.getSceneId() == null) {
            return coreChartView.getTitle();
        }
        List<DataVisualizationInfo> parents = getParents(coreChartView.getSceneId());
        StringBuilder stringBuilder = new StringBuilder();
        parents.forEach(ele -> {
            if (ObjectUtils.isNotEmpty(ele)) {
                stringBuilder.append(ele.getName()).append("/");
            }
        });
        stringBuilder.append(coreChartView.getTitle());
        return stringBuilder.toString();
    }

    public List<DataVisualizationInfo> getParents(Long id) {
        List<DataVisualizationInfo> list = new ArrayList<>();
        DataVisualizationInfo dataVisualizationInfo = visualizationInfoMapper.selectById(id);
        list.add(dataVisualizationInfo);
        getParent(list, dataVisualizationInfo);
        Collections.reverse(list);
        return list;
    }

    public void getParent(List<DataVisualizationInfo> list, DataVisualizationInfo dataVisualizationInfo) {
        if (ObjectUtils.isNotEmpty(dataVisualizationInfo)) {
            if (dataVisualizationInfo.getPid() != null) {
                DataVisualizationInfo d = visualizationInfoMapper.selectById(dataVisualizationInfo.getPid());
                list.add(d);
                getParent(list, d);
            }
        }
    }

}
