package io.dataease.service.panel;

import com.google.gson.Gson;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.constants.PanelConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.panel.PanelAppTemplateApplyRequest;
import io.dataease.controller.request.panel.PanelAppTemplateRequest;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateMapper;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.service.chart.ChartViewFieldService;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.datasource.DatasourceService;
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

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Service
public class PanelAppTemplateService {
    private static Gson gson = new Gson();

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

    public List<PanelAppTemplateWithBLOBs> list(PanelAppTemplateRequest request) {
        PanelAppTemplateExample example = new PanelAppTemplateExample();
        if (StringUtils.isNotEmpty(request.getPid())) {
            example.createCriteria().andPidEqualTo(request.getPid());
        }
        if (StringUtils.isNotEmpty(request.getNodeType())) {
            example.createCriteria().andNodeTypeEqualTo(request.getNodeType());
        }
        return panelAppTemplateMapper.selectByExampleWithBLOBs(example);
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
        panelAppTemplateMapper.insertSelective(requestTemplate);
    }


    public void update(PanelAppTemplateRequest request) {
        nameCheck(CommonConstants.OPT_TYPE.UPDATE, request.getName(), request.getPid(), request.getId());
        request.setUpdateUser(AuthUtils.getUser().getUsername());
        request.setUpdateTime(System.currentTimeMillis());
        PanelAppTemplateWithBLOBs requestTemplate = new PanelAppTemplateWithBLOBs();
        BeanUtils.copyBean(requestTemplate, request);
        panelAppTemplateMapper.updateByPrimaryKeySelective(requestTemplate);
    }

    public void delete(String appTemplateId) {
        panelAppTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    public String nameCheck(PanelAppTemplateRequest request) {
        return nameCheck(request.getOptType(), request.getName(), request.getPid(), request.getId());

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
    public Map<String, String> applyDatasource(List<Datasource> oldDatasourceList, List<Datasource> newDatasourceList) throws Exception {
        Map<String, String> datasourceRelaMap = new HashMap<>();
        for (int i = 0; i < newDatasourceList.size(); i++) {
            Datasource datasource = newDatasourceList.get(0);
            datasource.setId(null);
            Datasource newDatasource = datasourceService.addDatasource(datasource);
            datasourceRelaMap.put(oldDatasourceList.get(i).getId(), newDatasource.getId());
        }
        return datasourceRelaMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyPanelView(List<PanelView> panelViewsInfo, Map<String, String> chartViewsRelaMap, String panelId) {
        Long time = System.currentTimeMillis();
        String userName = AuthUtils.getUser().getUsername();
        panelViewsInfo.forEach(panelView -> {
            panelView.setId(UUIDUtil.getUUIDAsString());
            panelView.setPanelId(panelId);
            panelView.setCreateTime(time);
            panelView.setCreateBy(userName);
            panelView.setChartViewId(chartViewsRelaMap.get(panelView.getChartViewId()));
            panelViewService.save(panelView);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public String applyPanel(PanelGroupRequest panelInfo, Map<String, String> chartViewsRelaMap, String newPanelId, String panelName, String pid) {
        panelInfo.setId(newPanelId);
        panelInfo.setPid(pid);
        panelInfo.setName(panelName);
        panelInfo.setNodeType("panel");
        panelInfo.setPanelType("self");
        panelInfo.setCreateBy(AuthUtils.getUser().getUsername());
        panelInfo.setCreateTime(System.currentTimeMillis());
        panelGroupService.newPanelFromApp(panelInfo, chartViewsRelaMap);
        return newPanelId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyDataset(List<DatasetTable> datasetTablesInfo, Map<String, String> datasourceRelaMap, String sceneId) throws Exception {
        Map<String, String> datasetsRelaMap = new HashMap<>();
        for (DatasetTable datasetTable : datasetTablesInfo) {
            String oldId = datasetTable.getId();
            datasetTable.setId(null);
            datasetTable.setSceneId(sceneId);
            datasetTable.setDataSourceId(datasourceRelaMap.get(datasetTable.getDataSourceId()));
            DataSetTableRequest datasetRequest = new DataSetTableRequest();
            BeanUtils.copyBean(datasetRequest, datasetTable);
            datasetRequest.setOptFrom("appApply");
            datasetRequest.setSyncType("sync_now");
            DatasetTable newDataset = dataSetTableService.save(datasetRequest);
            datasetsRelaMap.put(oldId, newDataset.getId());
        }
        return datasetsRelaMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyDatasetField(List<DatasetTableField> datasetTableFieldsInfo, Map<String, String> datasetsRelaMap) {
        Map<String, String> datasetFieldsRelaMap = new HashMap<>();
        for (DatasetTableField datasetTableField : datasetTableFieldsInfo) {
            String oldId = datasetTableField.getId();
            datasetTableField.setTableId(datasetsRelaMap.get(datasetTableField.getTableId()));
            DatasetTableField newTableField = dataSetTableFieldsService.save(datasetTableField);
            datasetFieldsRelaMap.put(oldId, newTableField.getId());
        }
        return datasetFieldsRelaMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetCustomAndUnionDataset(List<DatasetTable> datasetTablesInfo, Map<String, String> datasetRelaMap, Map<String, String> datasetFieldsRelaMap) throws Exception {
        for (DatasetTable datasetTable : datasetTablesInfo) {
            if ((DatasetType.CUSTOM.name().equalsIgnoreCase(datasetTable.getType()) || DatasetType.UNION.name().equalsIgnoreCase(datasetTable.getType()))) {
                datasetRelaMap.forEach((k, v) -> {
                    datasetTable.setInfo(datasetTable.getInfo().replaceAll(k, v));
                });
                datasetFieldsRelaMap.forEach((k, v) -> {
                    datasetTable.setInfo(datasetTable.getInfo().replaceAll(k, v));
                });
                if (1 == datasetTable.getMode()) {
                    if (DatasetType.CUSTOM.name().equalsIgnoreCase(datasetTable.getType())) {
                        dataSetTableService.createAppCustomDorisView(datasetTable.getInfo(), datasetTable.getId());
                    } else if (DatasetType.UNION.name().equalsIgnoreCase(datasetTable.getType())) {
                        dataSetTableService.createAppUnionDorisView(datasetTable.getInfo(), datasetTable.getId());
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyViews(List<ChartViewWithBLOBs> chartViewsInfo, Map<String, String> datasetsRelaMap, Map<String, String> datasetFieldsRelaMap, String sceneId) throws Exception {
        Map<String, String> chartViewsRelaMap = new HashMap<>();
        for (ChartViewWithBLOBs chartView : chartViewsInfo) {
            String oldViewId = chartView.getId();
            // 替换datasetId
            chartView.setTableId(datasetsRelaMap.get(chartView.getTableId()));
            datasetsRelaMap.forEach((k, v) -> {
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
            datasetFieldsRelaMap.forEach((k, v) -> {
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
            chartViewsRelaMap.put(oldViewId, newOne.getId());
        }
        return chartViewsRelaMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> applyViewsField(List<ChartViewField> chartViewFieldsInfo, Map<String, String> chartViewsRelaMap, Map<String, String> datasetsRelaMap, Map<String, String> datasetFieldsRelaMap) {
        Map<String, String> chartViewFieldsRelaMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(chartViewFieldsInfo)) {
            for (ChartViewField chartViewField : chartViewFieldsInfo) {
                String oldChartFieldId = chartViewField.getId();
                chartViewField.setId(null);
                //替换datasetId
                chartViewField.setTableId(datasetsRelaMap.get(chartViewField.getTableId()));
                //替换chartViewId
                chartViewField.setChartId(chartViewsRelaMap.get(chartViewField.getId()));
                //替换datasetFieldId
                datasetFieldsRelaMap.forEach((k, v) -> {
                    chartViewField.setOriginName(chartViewField.getOriginName().replaceAll(k, v));
                });
                ChartViewField newChartViewField = chartViewFieldService.save(chartViewField);
                chartViewFieldsRelaMap.put(oldChartFieldId, newChartViewField.getId());
            }
        }
        return chartViewFieldsRelaMap;
    }

    public void nameCheck(PanelAppTemplateApplyRequest request) {
        panelGroupService.checkPanelName(request.getPanelName(), request.getPanelId(), PanelConstants.OPT_TYPE_INSERT, null, "panel");
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setPid(request.getDatasetGroupId());
        datasetGroup.setName(request.getDatasetGroupName());
        dataSetGroupService.checkName(datasetGroup);
        request.getDatasourceList().stream().forEach(datasource -> {
            datasourceService.checkName(datasource.getName(), datasource.getType(), null);
        });

    }
}
