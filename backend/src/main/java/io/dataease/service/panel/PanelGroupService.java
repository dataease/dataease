package io.dataease.service.panel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.base.mapper.ext.*;
import io.dataease.commons.constants.*;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.PanelGroupExtendDataDTO;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.dto.panel.linkJump.PanelLinkJumpBaseRequest;
import io.dataease.dto.panel.po.PanelViewInsertDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.sys.SysAuthService;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Service
public class PanelGroupService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Resource
    private PanelGroupMapper panelGroupMapper;
    @Resource
    private ExtPanelGroupMapper extPanelGroupMapper;
    @Resource
    private ChartViewService chartViewService;
    @Resource
    private ChartViewMapper chartViewMapper;
    @Resource
    private StoreService storeService;
    @Resource
    private ShareService shareService;
    @Resource
    private PanelLinkService panelLinkService;
    @Resource
    private SysAuthService sysAuthService;
    @Resource
    private PanelViewService panelViewService;
    @Resource
    private ExtPanelLinkJumpMapper extPanelLinkJumpMapper;
    @Resource
    private ExtVAuthModelMapper extVAuthModelMapper;
    @Resource
    private VAuthModelMapper vAuthModelMapper;
    @Resource
    private PanelViewMapper panelViewMapper;
    @Resource
    private ExtPanelViewMapper extPanelViewMapper;
    @Resource
    private ExtPanelViewLinkageMapper extPanelViewLinkageMapper;
    @Resource
    private ExtChartViewMapper extChartViewMapper;
    @Resource
    private ExtDataSetTableMapper extDataSetTableMapper;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private PanelTemplateMapper templateMapper;
    @Resource
    private ExtPanelGroupExtendDataMapper extPanelGroupExtendDataMapper;

    public List<PanelGroupDTO> tree(PanelGroupRequest panelGroupRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        panelGroupRequest.setUserId(userId);
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(panelGroupRequest);
        return TreeUtils.mergeTree(panelGroupDTOList, "panel_list");
    }

    public List<PanelGroupDTO> defaultTree(PanelGroupRequest panelGroupRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        panelGroupRequest.setUserId(userId);
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupListDefault(panelGroupRequest);
        return TreeUtils.mergeTree(panelGroupDTOList, "default_panel");
    }

    @DeCleaner(DePermissionType.PANEL)
    public PanelGroup saveOrUpdate(PanelGroupRequest request) {
        String userName = AuthUtils.getUser().getUsername();
        String panelId = request.getId();
        if(StringUtils.isNotEmpty(panelId)){
            panelViewService.syncPanelViews(request);
        }
        if (StringUtils.isEmpty(panelId)) { // 新建
            checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_INSERT, null, request.getNodeType());
            panelId = newPanel(request);
            panelGroupMapper.insert(request);
            // 清理权限缓存
            clearPermissionCache();
            sysAuthService.copyAuth(panelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
        } else if ("toDefaultPanel".equals(request.getOptType())) { // 转存为默认仪表板
            panelId = UUID.randomUUID().toString();
            PanelGroupWithBLOBs newDefaultPanel = panelGroupMapper.selectByPrimaryKey(request.getId());
            newDefaultPanel.setPanelType(PanelConstants.PANEL_TYPE_SYSTEM);
            newDefaultPanel.setNodeType(PanelConstants.PANEL_NODE_TYPE_PANEL);
            newDefaultPanel.setName(request.getName());
            newDefaultPanel.setId(panelId);
            newDefaultPanel.setPid(PanelConstants.PANEL_GATHER_DEFAULT_PANEL);
            newDefaultPanel.setLevel(0);
            newDefaultPanel.setSource(request.getId());
            newDefaultPanel.setCreateBy(AuthUtils.getUser().getUsername());
            checkPanelName(newDefaultPanel.getName(), newDefaultPanel.getPid(), PanelConstants.OPT_TYPE_INSERT, newDefaultPanel.getId(), newDefaultPanel.getNodeType());
            panelGroupMapper.insertSelective(newDefaultPanel);
            // 清理权限缓存
            clearPermissionCache();
            sysAuthService.copyAuth(panelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
        } else if ("copy".equals(request.getOptType())) {
            panelId = this.panelGroupCopy(request, null, true);
            // 清理权限缓存
            clearPermissionCache();
            sysAuthService.copyAuth(panelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
        } else if ("move".equals(request.getOptType())) {
            PanelGroupWithBLOBs panelInfo = panelGroupMapper.selectByPrimaryKey(request.getId());
            if (panelInfo.getPid().equalsIgnoreCase(request.getPid())) {
                DataEaseException.throwException(Translator.get("i18n_select_diff_folder"));
            }
            // 移动校验
            if (StringUtils.isNotEmpty(request.getName())) {
                checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_INSERT, request.getId(), panelInfo.getNodeType());
            }
            PanelGroupWithBLOBs record = new PanelGroupWithBLOBs();
            record.setName(request.getName());
            record.setId(request.getId());
            record.setPid(request.getPid());
            panelGroupMapper.updateByPrimaryKeySelective(record);

        } else {
            // 更新
            if (StringUtils.isNotEmpty(request.getName())) {
                checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_UPDATE, request.getId(), request.getNodeType());
            }
            panelGroupMapper.updateByPrimaryKeySelective(request);
        }

        //带有权限的返回
        PanelGroupRequest authRequest = new PanelGroupRequest();
        authRequest.setId(panelId);
        authRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(authRequest);
        if (!CollectionUtils.isNotEmpty(panelGroupDTOList)) {
            DataEaseException.throwException("未查询到用户对应的资源权限，请尝试刷新重新保存");
        }
        return panelGroupDTOList.get(0);
    }


    private void checkPanelName(String name, String pid, String optType, String id, String nodeType) {
        PanelGroupExample groupExample = new PanelGroupExample();
        if (PanelConstants.OPT_TYPE_INSERT.equalsIgnoreCase(optType)) {
            groupExample.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andNodeTypeEqualTo(nodeType);
        } else if (PanelConstants.OPT_TYPE_UPDATE.equalsIgnoreCase(optType)) {
            groupExample.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id).andNodeTypeEqualTo(nodeType);
        }

        List<PanelGroup> checkResult = panelGroupMapper.selectByExample(groupExample);
        if (CollectionUtils.isNotEmpty(checkResult)) {
            DataEaseException.throwException(Translator.get("i18n_same_folder_can_not_repeat"));
        }
    }


    public void deleteCircle(String id) {
        Assert.notNull(id, "id cannot be null");
        sysAuthService.checkTreeNoManageCount("panel", id);

        //清理view 和 view cache
        extPanelGroupMapper.deleteCircleView(id);
        extPanelGroupMapper.deleteCircleViewCache(id);

        // 同时会删除对应默认仪表盘
        extPanelGroupMapper.deleteCircle(id);
        storeService.removeByPanelId(id);
        shareService.delete(id, null);
        panelLinkService.deleteByResourceId(id);


        //清理跳转信息
        extPanelLinkJumpMapper.deleteJumpTargetViewInfoWithPanel(id);
        extPanelLinkJumpMapper.deleteJumpInfoWithPanel(id);
        extPanelLinkJumpMapper.deleteJumpWithPanel(id);
    }


    public PanelGroupDTO findOne(String panelId) {
        PanelGroupDTO panelGroup = extPanelGroupMapper.findOneWithPrivileges(panelId, String.valueOf(AuthUtils.getUser().getUserId()));
        // 默认仪表板取源仪表板样式
        if (panelGroup != null && StringUtils.isNotEmpty(panelGroup.getSource())) {
            PanelGroupDTO sourcePanel = extPanelGroupMapper.findOneWithPrivileges(panelGroup.getSource(), String.valueOf(AuthUtils.getUser().getUserId()));
            panelGroup.setPanelData(sourcePanel.getPanelData());
            panelGroup.setPanelStyle(sourcePanel.getPanelStyle());
            panelGroup.setSourcePanelName(sourcePanel.getName());
        }
        return panelGroup;
    }


    public List<ChartViewDTO> getUsableViews() throws Exception {
        List<ChartViewDTO> chartViewDTOList = new ArrayList<>();
        List<ChartView> allChartView = chartViewMapper.selectByExample(null);
        Optional.ofNullable(allChartView).orElse(new ArrayList<>()).forEach(chartView -> {
            try {
                chartViewDTOList.add(chartViewService.getData(chartView.getId(), null));
            } catch (Exception e) {
                LOGGER.error("获取view详情出错：" + chartView.getId(), e);
            }
        });
        return chartViewDTOList;
    }

    public List<VAuthModelDTO> queryPanelViewTree() {
        List<VAuthModelDTO> result = new ArrayList<>();
        VAuthModelRequest panelRequest = new VAuthModelRequest();
        panelRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        panelRequest.setModelType("panel");
        List<VAuthModelDTO> panelResult = extVAuthModelMapper.queryAuthModel(panelRequest);
        // 获取仪表板下面的视图
        if (CollectionUtils.isNotEmpty(panelResult)) {
            result.addAll(panelResult);
            List<String> panelIds = panelResult.stream().map(VAuthModelDTO::getId).collect(Collectors.toList());
            VAuthModelRequest viewRequest = new VAuthModelRequest();
            viewRequest.setPids(panelIds);
            List<VAuthModelDTO> viewResult = extVAuthModelMapper.queryAuthModelViews(viewRequest);
            if (CollectionUtils.isNotEmpty(viewResult)) {
                result.addAll(viewResult);
            }
            result = TreeUtils.mergeTree(result, "panel_list");
            if (AuthUtils.getUser().getIsAdmin()) {
                // 原有视图的目录结构
                List<VAuthModelDTO> viewOriginal = extVAuthModelMapper.queryAuthViewsOriginal(viewRequest);
                if (CollectionUtils.isNotEmpty(viewOriginal) && viewOriginal.size() > 1) {
                    result.addAll(TreeUtils.mergeTree(viewOriginal, "public_chart"));
                }
            }

        }
        return result;
    }

    public String panelGroupCopy(PanelGroupRequest request, String newPanelId, boolean checkName) {
        String sourcePanelId = request.getId(); //源仪表板ID
        if (StringUtils.isEmpty(newPanelId)) {
            newPanelId = UUIDUtil.getUUIDAsString(); //目标仪表板ID
        }
        String copyId = UUIDUtil.getUUIDAsString(); // 本次复制执行ID
        // 复制仪表板
        PanelGroupWithBLOBs newPanel = panelGroupMapper.selectByPrimaryKey(sourcePanelId);
        if (checkName && StringUtils.isNotEmpty(request.getName())) {
            // 插入校验
            checkPanelName(request.getName(), newPanel.getPid(), PanelConstants.OPT_TYPE_INSERT, request.getId(), newPanel.getNodeType());
        }
        newPanel.setName(request.getName());
        newPanel.setId(newPanelId);
        newPanel.setCreateBy(AuthUtils.getUser().getUsername());
        //TODO copy panelView
        extPanelViewMapper.copyFromPanel(newPanelId, sourcePanelId, copyId);
        //TODO 复制视图 chart_view
        extChartViewMapper.chartCopyWithPanel(copyId);
        //TODO 替换panel_data viewId 数据
        List<PanelView> panelViewList = panelViewService.findPanelViews(copyId);
        //TODO 复制模板缓存数据
        extPanelGroupExtendDataMapper.copyWithCopyId(copyId);
        if (CollectionUtils.isNotEmpty(panelViewList)) {
            String panelData = newPanel.getPanelData();
            //TODO 替换panel_data viewId 数据  并保存
            for (PanelView panelView : panelViewList) {
                panelData = panelData.replaceAll(panelView.getCopyFromView(), panelView.getChartViewId());
            }
            newPanel.setPanelData(panelData);
            //TODO 复制跳转信息 copy panel_link_jump panel_link_jump_info  panel_link_jump_target_view_info
            extPanelLinkJumpMapper.copyLinkJump(copyId);
            extPanelLinkJumpMapper.copyLinkJumpInfo(copyId);
            extPanelLinkJumpMapper.copyLinkJumpTarget(copyId);
            //TODO 复制联动信息 copy panel_view_linkage_field panel_view_linkage
            extPanelViewLinkageMapper.copyViewLinkage(copyId);
            extPanelViewLinkageMapper.copyViewLinkageField(copyId);
        }
        panelGroupMapper.insertSelective(newPanel);
        return newPanelId;
    }

    public String newPanel(PanelGroupRequest request){
        String newPanelId = UUIDUtil.getUUIDAsString();
        String newFrom = request.getNewFrom();
        String templateStyle = null;
        String templateData = null;
        String dynamicData = null;
        if(PanelConstants.NEW_PANEL_FROM.NEW.equals(newFrom)){

        }else{
            //内部模板新建
            if(PanelConstants.NEW_PANEL_FROM.NEW_INNER_TEMPLATE.equals(newFrom)){
                PanelTemplateWithBLOBs panelTemplate = templateMapper.selectByPrimaryKey(request.getTemplateId());
                templateStyle = panelTemplate.getTemplateStyle();
                templateData = panelTemplate.getTemplateData();
                dynamicData = panelTemplate.getDynamicData();
            }else if(PanelConstants.NEW_PANEL_FROM.NEW_OUTER_TEMPLATE.equals(newFrom)){
                templateStyle = request.getPanelStyle();
                templateData = request.getPanelData();
                dynamicData = request.getDynamicData();
            }
            Map<String,String> dynamicDataMap = JSON.parseObject(dynamicData,Map.class);
            List<PanelViewInsertDTO> panelViews = new ArrayList<>();
            List<PanelGroupExtendDataDTO> viewsData = new ArrayList<>();
            for(Map.Entry<String, String> entry : dynamicDataMap.entrySet()){
                String originViewId = entry.getKey();
                String originViewData = entry.getValue();
                ChartViewDTO chartView = JSON.parseObject(originViewData,ChartViewDTO.class);
                String position = chartView.getPosition();
                String newViewId = UUIDUtil.getUUIDAsString();
                chartView.setId(newViewId);
                chartView.setSceneId(newPanelId);
                chartView.setDataFrom(CommonConstants.VIEW_DATA_FROM.TEMPLATE);
                //TODO 数据处理 1.替换viewId 2.加入panelView 数据(数据来源为template) 3.加入模板view data数据
                templateData = templateData.replaceAll(originViewId,newViewId);
                panelViews.add(new PanelViewInsertDTO(newViewId,newPanelId,position));
                viewsData.add(new PanelGroupExtendDataDTO(newPanelId,newViewId,originViewData));
                chartViewMapper.insertSelective(chartView);
                extChartViewMapper.copyToCache(newViewId);
            }
            if(CollectionUtils.isNotEmpty(panelViews)){
                extPanelViewMapper.savePanelView(panelViews);
                extPanelGroupExtendDataMapper.savePanelExtendData(viewsData);
            }
            request.setPanelData(templateData);
            request.setPanelStyle(templateStyle);
        }
        request.setId(newPanelId);
        request.setCreateTime(System.currentTimeMillis());
        request.setCreateBy(AuthUtils.getUser().getUsername());
        return newPanelId;
    }

    public void sysInit1HistoryPanel() {
        LogUtil.info("=====v1.8版本 仪表板私有化【开始】=====");
        List<PanelGroupDTO> needInitPanels = extPanelGroupMapper.panelGroupInit();
        for (PanelGroupDTO panelGroupDTO : needInitPanels) {
            LogUtil.info("==>" + panelGroupDTO.getName() + "&" + panelGroupDTO.getId());
            String sourcePanelId = panelGroupDTO.getId(); //仪表板ID
            String copyId = UUIDUtil.getUUIDAsString(); // 本次复制执行ID
            //TODO copy panelView
            extPanelViewMapper.copyFromPanel(sourcePanelId, sourcePanelId, copyId);
            //TODO 复制视图 chart_view
            extChartViewMapper.chartCopyWithPanel(copyId);
            //TODO 替换panel_data viewId 数据
            List<PanelView> panelViewList = panelViewService.findPanelViews(copyId);
            String panelData = panelGroupDTO.getPanelData();
            if (CollectionUtils.isNotEmpty(panelViewList) && StringUtils.isNotEmpty(panelData)) {
                PanelView panelViewtemp = new PanelView();
                try {
                    //TODO 替换panel_data viewId 数据  并保存
                    for (PanelView panelView : panelViewList) {
                        panelViewtemp = panelView;
                        panelData = panelData.replaceAll(panelView.getCopyFromView(), panelView.getChartViewId());
                    }
                    panelGroupDTO.setPanelData(panelData);
                    panelGroupMapper.updateByPrimaryKeySelective(panelGroupDTO);
                    //TODO 复制跳转信息 copy panel_link_jump panel_link_jump_info  panel_link_jump_target_view_info
                    extPanelLinkJumpMapper.copyLinkJump(copyId);
                    extPanelLinkJumpMapper.copyLinkJumpInfo(copyId);
                    extPanelLinkJumpMapper.copyLinkJumpTarget(copyId);
                    //TODO 复制联动信息 copy panel_view_linkage_field panel_view_linkage
                    extPanelViewLinkageMapper.copyViewLinkage(copyId);
                    extPanelViewLinkageMapper.copyViewLinkageField(copyId);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("错误===》panel:" + panelGroupDTO.getId() + ";panelView:" + JSON.toJSONString(panelViewtemp));
                }
            }
        }
        //TODO 清理已经复制过的Panel_view
        PanelViewExample clearViewExample = new PanelViewExample();
        clearViewExample.createCriteria().andCopyFromIsNull();
        panelViewMapper.deleteByExample(clearViewExample);

        LogUtil.info("=====v1.8版本 仪表板私有化【结束】=====");
    }

    // 获取仪表板的视图信息
    public Map queryPanelComponents(String panelId) {
        try {
            Map result, tableWithFields, viewWithViewInfo, tableWithTableInfo;
            //查找所有view
            List<ChartViewDTO> views = extChartViewMapper.searchViewsWithPanelId(panelId);
            viewWithViewInfo = views.stream().collect(Collectors.toMap(ChartViewDTO::getId, ChartViewDTO -> ChartViewDTO));
            //查找所有dataset
            List<DataSetTableDTO> tables = extDataSetTableMapper.searchDataSetTableWithPanelId(panelId, String.valueOf(AuthUtils.getUser().getUserId()));
            tableWithTableInfo = tables.stream().collect(Collectors.toMap(DataSetTableDTO::getId, DataSetTableDTO -> DataSetTableDTO));
            //查找所有datasetFields
            tableWithFields = new HashMap();
            if (CollectionUtils.isNotEmpty(tables)) {
                for (DataSetTableDTO table : tables) {
                    DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
                    dataSetTableRequest.setId(table.getId());
                    Map<String, List<DatasetTableField>> tableDataSetFields = dataSetTableService.getFieldsFromDE(dataSetTableRequest);
                    tableWithFields.put(table.getId(), tableDataSetFields);
                }
            }

            result = new HashMap();
            result.put("tableWithFields", tableWithFields);
            result.put("viewWithViewInfo", viewWithViewInfo);
            result.put("tableWithTableInfo", tableWithTableInfo);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e);
        }
        return null;
    }
    private void clearPermissionCache(){
        CacheUtils.removeAll(AuthConstants.USER_PANEL_NAME);
        CacheUtils.removeAll(AuthConstants.ROLE_PANEL_NAME);
        CacheUtils.removeAll(AuthConstants.DEPT_PANEL_NAME);
    }

}
