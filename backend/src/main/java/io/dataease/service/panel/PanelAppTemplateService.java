package io.dataease.service.panel;

import com.google.gson.Gson;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.constants.PanelConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.TableUtils;
import io.dataease.controller.datasource.request.UpdataDsRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.panel.PanelAppTemplateApplyRequest;
import io.dataease.controller.request.panel.PanelAppTemplateRequest;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.ext.ExtPanelAppTemplateMapper;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.*;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.service.chart.ChartViewFieldService;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.ExtractDataService;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.staticResource.StaticResourceService;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.dataease.commons.constants.StaticResourceConstants.UPLOAD_URL_PREFIX;

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Service
public class PanelAppTemplateService {
    private static Gson gson = new Gson();

    @Resource
    private ExtPanelAppTemplateMapper extPanelAppTemplateMapper;
    @Resource
    private PanelAppTemplateMapper panelAppTemplateMapper;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private ChartViewService chartViewService;
    @Resource
    private ChartViewFieldService chartViewFieldService;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    @Lazy
    private PanelGroupService panelGroupService;
    @Resource
    private PanelViewService panelViewService;
    @Resource
    private DataSetGroupService dataSetGroupService;
    @Resource
    private StaticResourceService staticResourceService;
    @Resource
    private ExtractDataService extractDataService;
    @Resource
    private PanelLinkJumpMapper panelLinkJumpMapper;
    @Resource
    private PanelLinkJumpInfoMapper panelLinkJumpInfoMapper;
    @Resource
    private PanelViewLinkageMapper panelViewLinkageMapper;
    @Resource
    private PanelViewLinkageFieldMapper panelViewLinkageFieldMapper;

    public List<PanelAppTemplateWithBLOBs> list(PanelAppTemplateRequest request) {
        return extPanelAppTemplateMapper.queryBaseInfo(request.getNodeType(), request.getPid());
    }

    public void save(PanelAppTemplateRequest request) {
        request.setId(UUIDUtil.getUUIDAsString());
        request.setCreateUser(AuthUtils.getUser().getUsername());
        request.setCreateTime(System.currentTimeMillis());
        PanelAppTemplateWithBLOBs requestTemplate = new PanelAppTemplateWithBLOBs();
        BeanUtils.copyBean(requestTemplate, request);
        if (StringUtils.isEmpty(requestTemplate.getNodeType())) {
            requestTemplate.setNodeType("template");
        }
        if (StringUtils.isNotEmpty(request.getSnapshot())) {
            //Store static resource into the server
            String snapshotName = "app-template-" + request.getId() + ".jpeg";
            staticResourceService.saveSingleFileToServe(snapshotName, request.getSnapshot().replace("data:image/jpeg;base64,", ""));
            requestTemplate.setSnapshot("/" + UPLOAD_URL_PREFIX + '/' + snapshotName);
        }
        panelAppTemplateMapper.insertSelective(requestTemplate);
    }


    public void update(PanelAppTemplateRequest request) {
        nameCheck(CommonConstants.OPT_TYPE.UPDATE, request.getName(), request.getPid(), request.getId());
        request.setUpdateUser(AuthUtils.getUser().getUsername());
        request.setUpdateTime(System.currentTimeMillis());
        PanelAppTemplateWithBLOBs requestTemplate = new PanelAppTemplateWithBLOBs();
        BeanUtils.copyBean(requestTemplate, request);
        //Store static resource into the server
        if (StringUtils.isNotEmpty(request.getSnapshot()) && request.getSnapshot().indexOf("static-resource") == -1) {
            String snapshotName = "app-template-" + UUIDUtil.getUUIDAsString() + ".jpeg";
            staticResourceService.saveSingleFileToServe(snapshotName, request.getSnapshot().replace("data:image/jpeg;base64,", ""));
            requestTemplate.setSnapshot("/" + UPLOAD_URL_PREFIX + '/' + snapshotName);
        }
        panelAppTemplateMapper.updateByPrimaryKeySelective(requestTemplate);
    }

    public void delete(String appTemplateId) {
        panelAppTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    public String nameCheck(PanelAppTemplateRequest request) {
        return nameCheck(request.getOptType(), request.getName(), request.getPid(), request.getId());

    }

    public void move(PanelAppTemplateRequest request) {
        if (!CommonConstants.CHECK_RESULT.NONE.equals(nameCheck(CommonConstants.OPT_TYPE.INSERT, request.getName(), request.getPid(), request.getId()))) {
            throw new RuntimeException("当前名称在目标分类中已经存在！请选择其他分类或修改名称");
        }
        PanelAppTemplateWithBLOBs appTemplate = new PanelAppTemplateWithBLOBs();
        appTemplate.setId(request.getId());
        appTemplate.setPid(request.getPid());
        panelAppTemplateMapper.updateByPrimaryKeySelective(appTemplate);
    }

    //名称检查
    public String nameCheck(String optType, String name, String pid, String id) {
        PanelAppTemplateExample example = new PanelAppTemplateExample();
        if (CommonConstants.OPT_TYPE.INSERT.equals(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name);

        } else if (CommonConstants.OPT_TYPE.UPDATE.equals(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id);
        }
        List<PanelAppTemplate> panelTemplates = panelAppTemplateMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(panelTemplates)) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyDatasource(List<Datasource> oldDatasourceList, PanelAppTemplateApplyRequest request) throws Exception {
        Map<String, String> datasourceRealMap = new HashMap<>();
        if (PanelConstants.APP_DATASOURCE_FROM.HISTORY.equals(request.getDatasourceFrom())) {
            datasourceRealMap.put(oldDatasourceList.get(0).getId(), request.getDatasourceHistoryId());
        } else {
            List<DatasourceDTO> newDatasourceList = request.getDatasourceList();
            for (int i = 0; i < newDatasourceList.size(); i++) {
                DatasourceDTO datasource = newDatasourceList.get(0);
                datasource.setId(null);
                Datasource newDatasource = datasourceService.addDatasource(datasource);
                datasourceRealMap.put(oldDatasourceList.get(i).getId(), newDatasource.getId());
            }
        }
        return datasourceRealMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyPanelView(List<PanelView> panelViewsInfo, Map<String, String> chartViewsRealMap, String panelId) {
        Long time = System.currentTimeMillis();
        String userName = AuthUtils.getUser().getUsername();
        panelViewsInfo.forEach(panelView -> {
            panelView.setId(UUIDUtil.getUUIDAsString());
            panelView.setPanelId(panelId);
            panelView.setCreateTime(time);
            panelView.setCreateBy(userName);
            panelView.setChartViewId(chartViewsRealMap.get(panelView.getChartViewId()));
            panelViewService.save(panelView);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public String applyPanel(PanelGroupRequest panelInfo, Map<String, String> chartViewsRealMap, Map<String, String> datasetsRealMap, Map<String, String> datasetFieldsRealMap, String newPanelId, String panelName, String pid) {
        panelInfo.setId(newPanelId);
        panelInfo.setPid(pid);
        panelInfo.setName(panelName);
        panelInfo.setNodeType("panel");
        panelInfo.setPanelType("self");
        panelInfo.setCreateBy(AuthUtils.getUser().getUsername());
        panelInfo.setCreateTime(System.currentTimeMillis());
        datasetsRealMap.forEach((k, v) -> {
            panelInfo.setPanelData(panelInfo.getPanelData().replaceAll(k, v));
        });
        datasetFieldsRealMap.forEach((k, v) -> {
            panelInfo.setPanelData(panelInfo.getPanelData().replaceAll(k, v));
        });
        panelGroupService.newPanelFromApp(panelInfo, chartViewsRealMap);
        return newPanelId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyDataset(List<DatasetTable> datasetTablesInfo, Map<String, String> datasourceRealMap, String sceneId) throws Exception {
        Map<String, String> datasetsRealMap = new HashMap<>();
        for (DatasetTable datasetTable : datasetTablesInfo) {
            String oldId = datasetTable.getId();
            datasetTable.setId(null);
            datasetTable.setSceneId(sceneId);
            datasetTable.setDataSourceId(datasourceRealMap.get(datasetTable.getDataSourceId()));
            DataSetTableRequest datasetRequest = new DataSetTableRequest();
            BeanUtils.copyBean(datasetRequest, datasetTable);
            datasetRequest.setOptFrom("appApply");
            datasetRequest.setSyncType("sync_now");
            DatasetTable newDataset = dataSetTableService.save(datasetRequest);
            datasetTable.setId(newDataset.getId());
            datasetsRealMap.put(oldId, newDataset.getId());
        }
        return datasetsRealMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyDatasetField(List<DatasetTableField> datasetTableFieldsInfo, Map<String, String> datasetsRealMap, Map<String, String> datasetTypeRealMap, Map<String, String> datasetFieldsMd5FormatRealMap) {
        Map<String, String> datasetFieldsRealMap = new HashMap<>();
        for (DatasetTableField datasetTableField : datasetTableFieldsInfo) {
            if (datasetTableField.getExtField() != 2) {
                String oldId = datasetTableField.getId();
                String oldTableId = datasetTableField.getTableId();
                datasetTableField.setTableId(datasetsRealMap.get(oldTableId));
                datasetTableField.setId(null);
                DatasetTableField newTableField = dataSetTableFieldsService.save(datasetTableField);
                datasetFieldsRealMap.put(oldId, newTableField.getId());
                datasetFieldsMd5FormatRealMap.put(TableUtils.fieldNameShort(oldTableId + "_" + datasetTableField.getOriginName()), TableUtils.fieldNameShort(newTableField.getTableId() + "_" + datasetTableField.getOriginName()));
                datasetFieldsMd5FormatRealMap.put(TableUtils.fieldName(oldTableId + "_" + datasetTableField.getDataeaseName()), TableUtils.fieldName(newTableField.getTableId() + "_" + datasetTableField.getDataeaseName()));
            }
        }
        //数据集计算字段替换
        for (DatasetTableField datasetTableField : datasetTableFieldsInfo) {
            if (datasetTableField.getExtField() == 2) {
                String oldId = datasetTableField.getId();
                String oldTableId = datasetTableField.getTableId();
                String oldOriginName = datasetTableField.getOriginName();
                datasetTableField.setTableId(datasetsRealMap.get(oldTableId));
                datasetTableField.setId(null);
                datasetFieldsRealMap.forEach((k, v) -> {
                    datasetTableField.setOriginName(datasetTableField.getOriginName().replaceAll(k, v));
                });
                DatasetTableField newTableField = dataSetTableFieldsService.save(datasetTableField);
                datasetFieldsRealMap.put(oldId, newTableField.getId());
                datasetFieldsMd5FormatRealMap.put(TableUtils.fieldNameShort(oldTableId + "_" + oldOriginName), TableUtils.fieldNameShort(newTableField.getTableId() + "_" + datasetTableField.getOriginName()));
                datasetFieldsMd5FormatRealMap.put(TableUtils.fieldName(oldTableId + "_" + datasetTableField.getDataeaseName()), TableUtils.fieldName(newTableField.getTableId() + "_" + datasetTableField.getDataeaseName()));

            }
        }

        //custom 和 union originName替换
        for (DatasetTableField datasetTableField : datasetTableFieldsInfo) {
            if (DatasetType.UNION.name().equalsIgnoreCase(datasetTypeRealMap.get(datasetTableField.getTableId())) || DatasetType.CUSTOM.name().equalsIgnoreCase(datasetTypeRealMap.get(datasetTableField.getTableId()))) {
                DatasetTableField updateField = new DatasetTableField();
                updateField.setId(datasetTableField.getId());
                String newOriginName = datasetFieldsMd5FormatRealMap.get(datasetTableField.getOriginName());
                String dataeaseName = datasetFieldsMd5FormatRealMap.get(datasetTableField.getDataeaseName());
                if (StringUtils.isNotEmpty(newOriginName) || StringUtils.isNotEmpty(dataeaseName)) {
                    updateField.setOriginName(datasetFieldsMd5FormatRealMap.get(datasetTableField.getOriginName()));
                    updateField.setDataeaseName(datasetFieldsMd5FormatRealMap.get(datasetTableField.getDataeaseName()));
                    dataSetTableFieldsService.updateByPrimaryKeySelective(updateField);
                }
            }
        }
        return datasetFieldsRealMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createDorisTable(List<DatasetTable> datasetTablesInfo) throws Exception {
        for (DatasetTable datasetTable : datasetTablesInfo) {
            if (1 == datasetTable.getMode() && !(DatasetType.CUSTOM.name().equalsIgnoreCase(datasetTable.getType()) || DatasetType.UNION.name().equalsIgnoreCase(datasetTable.getType()))) {
                List<DatasetTableField> fields = extractDataService.getDatasetTableFields(datasetTable.getId());
                extractDataService.createEngineTable(TableUtils.tableName(datasetTable.getId()), fields);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetCustomAndUnionDataset(List<DatasetTable> datasetTablesInfo, Map<String, String> datasetRealMap, Map<String, String> datasetFieldsRealMap) throws Exception {
        for (DatasetTable datasetTable : datasetTablesInfo) {
            if ((DatasetType.CUSTOM.name().equalsIgnoreCase(datasetTable.getType()) || DatasetType.UNION.name().equalsIgnoreCase(datasetTable.getType()))) {
                datasetRealMap.forEach((k, v) -> {
                    datasetTable.setInfo(datasetTable.getInfo().replaceAll(k, v));
                });
                datasetFieldsRealMap.forEach((k, v) -> {
                    datasetTable.setInfo(datasetTable.getInfo().replaceAll(k, v));
                });
                if (1 == datasetTable.getMode()) {
                    if (DatasetType.CUSTOM.name().equalsIgnoreCase(datasetTable.getType())) {
                        dataSetTableService.createAppCustomDorisView(datasetTable.getInfo(), datasetTable.getId());
                    } else if (DatasetType.UNION.name().equalsIgnoreCase(datasetTable.getType())) {
                        dataSetTableService.createAppUnionDorisView(datasetTable.getInfo(), datasetTable.getId());
                    }
                } else {
                    dataSetTableService.updateDatasetInfo(datasetTable);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyViews(List<ChartViewWithBLOBs> chartViewsInfo, Map<String, String> datasetsRealMap, Map<String, String> datasetFieldsRealMap, Map<String, String> datasetFieldsMd5FormatRealMap, String sceneId) throws Exception {
        Map<String, String> chartViewsRealMap = new HashMap<>();
        for (ChartViewWithBLOBs chartView : chartViewsInfo) {
            String oldViewId = chartView.getId();
            // 替换datasetId
            chartView.setTableId(datasetsRealMap.get(chartView.getTableId()));
            datasetsRealMap.forEach((k, v) -> {
                chartView.setXAxis(chartView.getXAxis().replaceAll(k, v));
                chartView.setXAxisExt(chartView.getXAxisExt().replaceAll(k, v));
                chartView.setYAxis(chartView.getYAxis().replaceAll(k, v));
                chartView.setYAxisExt(chartView.getYAxisExt().replaceAll(k, v));
                chartView.setExtStack(chartView.getExtStack().replaceAll(k, v));
                chartView.setExtBubble(chartView.getExtBubble().replaceAll(k, v));
                chartView.setCustomAttr(chartView.getCustomAttr().replaceAll(k, v));
                chartView.setCustomStyle(chartView.getCustomStyle().replaceAll(k, v));
                chartView.setCustomFilter(chartView.getCustomFilter().replaceAll(k, v));
                chartView.setDrillFields(chartView.getDrillFields().replaceAll(k, v));
            });
            //替换datasetFieldId
            datasetFieldsRealMap.forEach((k, v) -> {
                chartView.setXAxis(chartView.getXAxis().replaceAll(k, v));
                chartView.setXAxisExt(chartView.getXAxisExt().replaceAll(k, v));
                chartView.setYAxis(chartView.getYAxis().replaceAll(k, v));
                chartView.setYAxisExt(chartView.getYAxisExt().replaceAll(k, v));
                chartView.setExtStack(chartView.getExtStack().replaceAll(k, v));
                chartView.setExtBubble(chartView.getExtBubble().replaceAll(k, v));
                chartView.setCustomAttr(chartView.getCustomAttr().replaceAll(k, v));
                chartView.setCustomStyle(chartView.getCustomStyle().replaceAll(k, v));
                chartView.setCustomFilter(chartView.getCustomFilter().replaceAll(k, v));
                chartView.setDrillFields(chartView.getDrillFields().replaceAll(k, v));
            });
            //替换originName
            datasetFieldsMd5FormatRealMap.forEach((k, v) -> {
                chartView.setXAxis(chartView.getXAxis().replaceAll(k, v));
                chartView.setXAxisExt(chartView.getXAxisExt().replaceAll(k, v));
                chartView.setYAxis(chartView.getYAxis().replaceAll(k, v));
                chartView.setYAxisExt(chartView.getYAxisExt().replaceAll(k, v));
                chartView.setExtStack(chartView.getExtStack().replaceAll(k, v));
                chartView.setExtBubble(chartView.getExtBubble().replaceAll(k, v));
                chartView.setCustomAttr(chartView.getCustomAttr().replaceAll(k, v));
                chartView.setCustomStyle(chartView.getCustomStyle().replaceAll(k, v));
                chartView.setCustomFilter(chartView.getCustomFilter().replaceAll(k, v));
                chartView.setDrillFields(chartView.getDrillFields().replaceAll(k, v));
            });
            chartView.setId(null);
            chartView.setSceneId(sceneId);
            ChartViewWithBLOBs newOne = chartViewService.newOne(chartView);
            chartViewsRealMap.put(oldViewId, newOne.getId());
        }
        return chartViewsRealMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyViewsField(List<ChartViewField> chartViewFieldsInfo, Map<String, String> chartViewsRealMap, Map<String, String> datasetsRealMap, Map<String, String> datasetFieldsRealMap) {
        Map<String, String> chartViewFieldsRealMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(chartViewFieldsInfo)) {
            for (ChartViewField chartViewField : chartViewFieldsInfo) {
                String oldChartFieldId = chartViewField.getId();
                chartViewField.setId(null);
                //替换datasetId
                chartViewField.setTableId(datasetsRealMap.get(chartViewField.getTableId()));
                //替换chartViewId
                chartViewField.setChartId(chartViewsRealMap.get(chartViewField.getChartId()));
                //替换datasetFieldId
                datasetFieldsRealMap.forEach((k, v) -> {
                    chartViewField.setOriginName(chartViewField.getOriginName().replaceAll(k, v));
                });
                ChartViewField newChartViewField = chartViewFieldService.save(chartViewField);
                chartViewFieldsRealMap.put(oldChartFieldId, newChartViewField.getId());
            }
        }
        return chartViewFieldsRealMap;
    }

    public void nameCheck(PanelAppTemplateApplyRequest request, String optType) {
        if ("add".equals(optType)) {
            panelGroupService.checkPanelName(request.getPanelName(), request.getPanelGroupPid(), PanelConstants.OPT_TYPE_INSERT, null, "panel");
            DatasetGroup datasetGroup = new DatasetGroup();
            datasetGroup.setPid(request.getDatasetGroupPid());
            datasetGroup.setName(request.getDatasetGroupName());
            dataSetGroupService.checkName(datasetGroup);
            if (PanelConstants.APP_DATASOURCE_FROM.NEW.equals(request.getDatasourceFrom())) {
                request.getDatasourceList().stream().forEach(datasource -> {
                    datasourceService.checkName(datasource.getName(), datasource.getType(), null);
                });
            }
        } else {
            DatasetGroup datasetGroup = new DatasetGroup();
            datasetGroup.setPid(request.getDatasetGroupPid());
            datasetGroup.setName(request.getDatasetGroupName());
            datasetGroup.setId(request.getDatasetGroupId());
            dataSetGroupService.checkName(datasetGroup);
            if (PanelConstants.APP_DATASOURCE_FROM.NEW.equals(request.getDatasourceFrom())) {
                request.getDatasourceList().stream().forEach(datasource -> {
                    datasourceService.checkName(datasource.getName(), datasource.getType(), datasource.getId());
                });
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void editDatasource(List<DatasourceDTO> updateDatasourceList) throws Exception {
        for (int i = 0; i < updateDatasourceList.size(); i++) {
            UpdataDsRequest updataDsRequest = new UpdataDsRequest();
            BeanUtils.copyBean(updataDsRequest, updateDatasourceList.get(i));
            datasourceService.updateDatasource(updataDsRequest);

        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> applyLinkJumps(List<PanelLinkJump> linkJumps, Map<String, String> chartViewsRealMap, String newPanelId) {
        Map<String,String> linkJumpIdMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(linkJumps)){
            for(PanelLinkJump linkJump :linkJumps){
                String newLinkJumpId = UUIDUtil.getUUIDAsString();
                linkJumpIdMap.put(linkJump.getId(),newLinkJumpId);
                linkJump.setId(newLinkJumpId);
                linkJump.setSourcePanelId(newPanelId);
                linkJump.setSourceViewId(chartViewsRealMap.get(linkJump.getSourceViewId()));
                panelLinkJumpMapper.insertSelective(linkJump);
            }
        }
        return linkJumpIdMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyLinkJumpInfos(List<PanelLinkJumpInfo> linkJumpInfos, Map<String, String> linkJumpIdMap, Map<String, String> datasetFieldsRealMap) {
        if(!CollectionUtils.isEmpty(linkJumpInfos)){
            for(PanelLinkJumpInfo linkJumpInfo :linkJumpInfos){
                String newLinkJumpInfoId = UUIDUtil.getUUIDAsString();
                linkJumpInfo.setId(newLinkJumpInfoId);
                linkJumpInfo.setLinkJumpId(linkJumpIdMap.get(linkJumpInfo.getLinkJumpId()));
                linkJumpInfo.setSourceFieldId(datasetFieldsRealMap.get(linkJumpInfo.getSourceFieldId()));
                datasetFieldsRealMap.forEach((k, v) -> {
                    linkJumpInfo.setContent(linkJumpInfo.getContent().replaceAll(k, v));
                });
                panelLinkJumpInfoMapper.insertSelective(linkJumpInfo);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> applyLinkages(List<PanelViewLinkage> linkages, Map<String, String> chartViewsRealMap, String newPanelId) {
        Map<String,String> linkageIdMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(linkages)){
            for(PanelViewLinkage linkage :linkages){
                String newId = UUIDUtil.getUUIDAsString();
                linkageIdMap.put(linkage.getId(),newId);
                linkage.setId(newId);
                linkage.setPanelId(newPanelId);
                linkage.setSourceViewId(chartViewsRealMap.get(linkage.getSourceViewId()));
                linkage.setTargetViewId(chartViewsRealMap.get(linkage.getTargetViewId()));
                panelViewLinkageMapper.insertSelective(linkage);
            }
        }
        return linkageIdMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyLinkageFields(List<PanelViewLinkageField> linkageFields, Map<String, String> linkageIdMap, Map<String, String> datasetFieldsRealMap) {
        if(!CollectionUtils.isEmpty(linkageFields)){
            for(PanelViewLinkageField linkageField :linkageFields){
                String newId = UUIDUtil.getUUIDAsString();
                linkageField.setId(newId);
                linkageField.setLinkageId(linkageIdMap.get(linkageField.getLinkageId()));
                linkageField.setSourceField(datasetFieldsRealMap.get(linkageField.getSourceField()));
                linkageField.setTargetField(datasetFieldsRealMap.get(linkageField.getTargetField()));
                panelViewLinkageFieldMapper.insertSelective(linkageField);
            }
        }
    }
}
