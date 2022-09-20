package io.dataease.service.panel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.commons.constants.*;
import io.dataease.commons.utils.*;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.panel.*;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.PanelGroupExtendDataDTO;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.dto.panel.PanelExport2App;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.dto.panel.PanelTemplateFileDTO;
import io.dataease.dto.panel.po.PanelViewInsertDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.ext.*;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.*;
import io.dataease.plugins.common.constants.DeTypeConstants;
import io.dataease.service.chart.ChartGroupService;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.staticResource.StaticResourceService;
import io.dataease.service.sys.SysAuthService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.pentaho.di.core.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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

    private final Gson gson = new Gson();

    private final SysLogConstants.SOURCE_TYPE sourceType = SysLogConstants.SOURCE_TYPE.PANEL;

    private final static String DATA_URL_TITLE = "data:image/jpeg;base64,";
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
    @Resource
    private StaticResourceService staticResourceService;
    @Resource
    private ExtChartViewFieldMapper extChartViewFieldMapper;
    @Resource
    private ExtDataSetTableFieldMapper extDataSetTableFieldMapper;
    @Resource
    private ExtDataSetTaskMapper extDataSetTaskMapper;
    @Resource
    private ExtDataSourceMapper extDataSourceMapper;
    @Resource
    private PanelAppTemplateService panelAppTemplateService;
    @Resource
    private PanelAppTemplateMapper panelAppTemplateMapper;
    @Resource
    private PanelAppTemplateLogService appTemplateLogService;
    @Resource
    private ChartGroupService chartGroupService;
    @Resource
    private DataSetGroupService dataSetGroupService;

    public List<PanelGroupDTO> tree(PanelGroupRequest panelGroupRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        panelGroupRequest.setUserId(userId);
        panelGroupRequest.setIsAdmin(AuthUtils.getUser().getIsAdmin());
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(panelGroupRequest);
        return TreeUtils.mergeTree(panelGroupDTOList, "panel_list");
    }

    public List<PanelGroupDTO> defaultTree(PanelGroupRequest panelGroupRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        panelGroupRequest.setUserId(userId);
        panelGroupRequest.setIsAdmin(AuthUtils.getUser().getIsAdmin());
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupListDefault(panelGroupRequest);
        return TreeUtils.mergeTree(panelGroupDTOList, "default_panel");
    }

    @DeCleaner(value = DePermissionType.PANEL, key = "pid")
    public String save(PanelGroupRequest request) {
        checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_INSERT, null, request.getNodeType());
        String panelId = newPanel(request);
        panelGroupMapper.insertSelective(request);
        // 清理权限缓存
        clearPermissionCache();
        sysAuthService.copyAuth(panelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, sourceType, panelId, request.getPid(), null, null);
        this.removePanelAllCache(panelId);
        return panelId;
    }


    public String update(PanelGroupRequest request) {
        String panelId = request.getId();
        request.setUpdateTime(System.currentTimeMillis());
        request.setUpdateBy(AuthUtils.getUser().getUsername());
        panelViewService.syncPanelViews(request);
        if ("toDefaultPanel".equals(request.getOptType())) { // 转存为默认仪表板
            panelId = UUID.randomUUID().toString();
            PanelGroupWithBLOBs newDefaultPanel = panelGroupMapper.selectByPrimaryKey(request.getId());
            newDefaultPanel.setPanelType(PanelConstants.PANEL_TYPE.SYSTEM);
            newDefaultPanel.setNodeType(PanelConstants.PANEL_NODE_TYPE_PANEL);
            newDefaultPanel.setName(request.getName());
            newDefaultPanel.setId(panelId);
            newDefaultPanel.setPid(PanelConstants.PANEL_GATHER_DEFAULT_PANEL);
            newDefaultPanel.setLevel(0);
            newDefaultPanel.setSource(request.getId());
            newDefaultPanel.setCreateBy(AuthUtils.getUser().getUsername());
            newDefaultPanel.setCreateTime(System.currentTimeMillis());
            checkPanelName(newDefaultPanel.getName(), newDefaultPanel.getPid(), PanelConstants.OPT_TYPE_INSERT, newDefaultPanel.getId(), newDefaultPanel.getNodeType());
            panelGroupMapper.insertSelective(newDefaultPanel);
            // 清理权限缓存
            clearPermissionCache();
            sysAuthService.copyAuth(panelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, sourceType, panelId, PanelConstants.PANEL_GATHER_DEFAULT_PANEL, null, null);
        } else if ("copy".equals(request.getOptType())) {
            panelId = this.panelGroupCopy(request, null, true);
            // 清理权限缓存
            clearPermissionCache();
            sysAuthService.copyAuth(panelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
            if (StringUtils.isBlank(request.getPid())) {
                PanelGroupWithBLOBs panel = panelGroupMapper.selectByPrimaryKey(request.getId());
                if (ObjectUtils.isNotEmpty(panel)) {
                    request.setPid(panel.getPid());
                }
            }
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, sourceType, panelId, request.getPid(), null, null);
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
            record.setUpdateTime(request.getUpdateTime());
            record.setUpdateBy(request.getUpdateBy());
            panelGroupMapper.updateByPrimaryKeySelective(record);
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, sourceType, request.getId(), panelInfo.getPid(), request.getPid(), sourceType);

        } else {
            // 更新
            if (StringUtils.isNotEmpty(request.getName())) {
                checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_UPDATE, request.getId(), request.getNodeType());
            }
            panelGroupMapper.updateByPrimaryKeySelective(request);
            if (StringUtils.isBlank(request.getPid())) {
                PanelGroupWithBLOBs panel = panelGroupMapper.selectByPrimaryKey(request.getId());
                if (ObjectUtils.isNotEmpty(panel)) {
                    request.setPid(panel.getPid());
                }
            }
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, sourceType, request.getId(), request.getPid(), null, sourceType);
        }
        this.removePanelAllCache(panelId);
        return panelId;
    }


    public void checkPanelName(String name, String pid, String optType, String id, String nodeType) {
        PanelGroupExample groupExample = new PanelGroupExample();
        if (PanelConstants.OPT_TYPE_INSERT.equalsIgnoreCase(optType)) {
            groupExample.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andNodeTypeEqualTo(nodeType);
        } else if (PanelConstants.OPT_TYPE_UPDATE.equalsIgnoreCase(optType)) {
            groupExample.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id).andNodeTypeEqualTo(nodeType);
        }

        List<PanelGroup> checkResult = panelGroupMapper.selectByExample(groupExample);
        if (CollectionUtils.isNotEmpty(checkResult)) {
            DataEaseException.throwException(Translator.get("I18N_PANEL_EXIST"));
        }
    }


    public void deleteCircle(String id) {
        Assert.notNull(id, "id cannot be null");
        sysAuthService.checkTreeNoManageCount("panel", id);
        PanelGroupWithBLOBs panel = panelGroupMapper.selectByPrimaryKey(id);
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, sourceType, panel.getId(), panel.getPid(), null, null);
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
        DeLogUtils.save(sysLogDTO);
    }


    /**
     * @param panelId
     * @return
     * @Description 查询仪表板信息
     */
    public PanelGroupDTO findOne(String panelId) {
        Assert.notNull(panelId, "Method findOne panelId can not be null");
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

    public List<VAuthModelDTO> queryPanelMultiplexingViewTree() {
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
            // Version 1.11 only gets the current panel
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
        newPanel.setCreateTime(System.currentTimeMillis());
        // copy panelView
        extPanelViewMapper.copyFromPanel(newPanelId, sourcePanelId, copyId);
        // 复制视图 chart_view
        extChartViewMapper.chartCopyWithPanel(copyId);
        // 复制视图字段 chart_view_field
        extChartViewMapper.chartFiledCopyWithPanel(copyId);
        // 替换panel_data viewId 数据
        List<PanelView> panelViewList = panelViewService.findPanelViews(copyId);
        // 复制模板缓存数据
        extPanelGroupExtendDataMapper.copyWithCopyId(copyId);
        if (CollectionUtils.isNotEmpty(panelViewList)) {
            String panelData = newPanel.getPanelData();
            // 替换panel_data viewId 数据  并保存
            for (PanelView panelView : panelViewList) {
                panelData = panelData.replaceAll(panelView.getCopyFromView(), panelView.getChartViewId());
            }
            newPanel.setPanelData(panelData);
            // 复制跳转信息 copy panel_link_jump panel_link_jump_info  panel_link_jump_target_view_info
            extPanelLinkJumpMapper.copyLinkJump(copyId);
            extPanelLinkJumpMapper.copyLinkJumpInfo(copyId);
            extPanelLinkJumpMapper.copyLinkJumpTarget(copyId);
            // 复制联动信息 copy panel_view_linkage_field panel_view_linkage
            extPanelViewLinkageMapper.copyViewLinkage(copyId);
            extPanelViewLinkageMapper.copyViewLinkageField(copyId);
        }
        panelGroupMapper.insertSelective(newPanel);
        return newPanelId;
    }
    @Transactional(rollbackFor = Exception.class)
    public String newPanelFromApp(PanelGroupRequest request,Map<String,String> chartViewsRelaMap){
        String newPanelId = request.getId();
        String templateData = request.getPanelData();
        String staticResource = request.getStaticResource();
        Boolean mobileLayout = panelViewService.havaMobileLayout(templateData);
        for(Map.Entry<String,String> entry:chartViewsRelaMap.entrySet()){
            templateData =  templateData.replaceAll(entry.getKey(),entry.getValue());
        }
        request.setMobileLayout(mobileLayout);
        request.setPanelData(templateData);
        staticResourceService.saveFilesToServe(staticResource);
        panelGroupMapper.insertSelective(request);
        // 清理权限缓存
        clearPermissionCache();
        sysAuthService.copyAuth(newPanelId, SysAuthConstants.AUTH_SOURCE_TYPE_PANEL);
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, sourceType, newPanelId, request.getPid(), null, null);
        this.removePanelAllCache(newPanelId);
        return newPanelId;
    }

    public String newPanel(PanelGroupRequest request) {

        String newPanelId = UUIDUtil.getUUIDAsString();
        String newFrom = request.getNewFrom();
        String templateStyle = null;
        String templateData = null;
        String dynamicData = null;
        String staticResource = null;
        Boolean mobileLayout = false;
        if (PanelConstants.NEW_PANEL_FROM.NEW.equals(newFrom)) {
            // do nothing
        } else {
            //内部模板新建
            if (PanelConstants.NEW_PANEL_FROM.NEW_INNER_TEMPLATE.equals(newFrom)) {
                PanelTemplateWithBLOBs panelTemplate = templateMapper.selectByPrimaryKey(request.getTemplateId());
                templateStyle = panelTemplate.getTemplateStyle();
                templateData = panelTemplate.getTemplateData();
                dynamicData = panelTemplate.getDynamicData();
                mobileLayout = panelViewService.havaMobileLayout(templateData);
            } else if (PanelConstants.NEW_PANEL_FROM.NEW_OUTER_TEMPLATE.equals(newFrom)) {
                templateStyle = request.getPanelStyle();
                templateData = request.getPanelData();
                dynamicData = request.getDynamicData();
                staticResource = request.getStaticResource();
                mobileLayout = panelViewService.havaMobileLayout(templateData);
            } else if (PanelConstants.NEW_PANEL_FROM.NEW_MARKET_TEMPLATE.equals(newFrom)) {
                PanelTemplateFileDTO templateFileInfo = getTemplateFromMarket(request.getTemplateUrl());
                if (templateFileInfo == null) {
                    DataEaseException.throwException("Can't find the template's info from market,please check");
                }
                templateStyle = templateFileInfo.getPanelStyle();
                templateData = templateFileInfo.getPanelData();
                dynamicData = templateFileInfo.getDynamicData();
                staticResource = templateFileInfo.getStaticResource();
                mobileLayout = panelViewService.havaMobileLayout(templateData);
            }
            Map<String, String> dynamicDataMap = gson.fromJson(dynamicData, Map.class);
            if (dynamicDataMap == null) {
                DataEaseException.throwException("Please use the template after v1.9");
            }

            List<PanelViewInsertDTO> panelViews = new ArrayList<>();
            List<PanelGroupExtendDataDTO> viewsData = new ArrayList<>();
            for (Map.Entry<String, String> entry : dynamicDataMap.entrySet()) {
                String originViewId = entry.getKey();
                String originViewData = entry.getValue();
                ChartViewDTO chartView = gson.fromJson(originViewData, ChartViewDTO.class);
                chartView = transferTemplateMapAreaCode(chartView);
                String position = chartView.getPosition();
                String newViewId = UUIDUtil.getUUIDAsString();
                chartView.setId(newViewId);
                chartView.setSceneId(newPanelId);
                chartView.setDataFrom(CommonConstants.VIEW_DATA_FROM.TEMPLATE);
                // 数据处理 1.替换viewId 2.加入panelView 数据(数据来源为template) 3.加入模板view data数据
                templateData = templateData.replaceAll(originViewId, newViewId);
                panelViews.add(new PanelViewInsertDTO(newViewId, newPanelId, position));
                viewsData.add(new PanelGroupExtendDataDTO(newPanelId, newViewId, originViewData));
                chartViewMapper.insertSelective(chartView);
                extChartViewMapper.copyToCache(newViewId);
            }
            if (CollectionUtils.isNotEmpty(panelViews)) {
                extPanelViewMapper.savePanelView(panelViews);
                extPanelGroupExtendDataMapper.savePanelExtendData(viewsData);
            }
            request.setPanelData(templateData);
            request.setPanelStyle(templateStyle);
            //Store static resource into the server
            staticResourceService.saveFilesToServe(staticResource);
        }
        request.setId(newPanelId);
        request.setCreateTime(System.currentTimeMillis());
        request.setCreateBy(AuthUtils.getUser().getUsername());
        request.setMobileLayout(mobileLayout);
        return newPanelId;
    }

    private ChartViewDTO transferTemplateMapAreaCode(ChartViewDTO chartViewDTO) {
        Object areaCodeObj = null;
        String areaCodeKey = "areaCode";
        if (StringUtils.equals(chartViewDTO.getType(), "map") || StringUtils.equals(chartViewDTO.getType(), "buddle-map")) {
            String customAttrJson = chartViewDTO.getCustomAttr();
            Map map = gson.fromJson(customAttrJson, Map.class);
            if (map.containsKey(areaCodeKey) && ObjectUtils.isNotEmpty((areaCodeObj = map.get(areaCodeKey))) && areaCodeObj.toString().length() == 6) {
                map.put(areaCodeKey, "156" + areaCodeObj.toString());
                chartViewDTO.setCustomAttr(gson.toJson(map));
            }
        }
        return chartViewDTO;
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

    private void clearPermissionCache() {
        CacheUtils.removeAll(AuthConstants.USER_PANEL_NAME);
        CacheUtils.removeAll(AuthConstants.ROLE_PANEL_NAME);
        CacheUtils.removeAll(AuthConstants.DEPT_PANEL_NAME);
    }


    public void exportPanelViewDetails(PanelViewDetailsRequest request, HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        try {
            String snapshot = request.getSnapshot();
            List<String[]> details = request.getDetails();
            Integer[] excelTypes = request.getExcelTypes();
            details.add(0, request.getHeader());
            HSSFWorkbook wb = new HSSFWorkbook();
            //明细sheet
            HSSFSheet detailsSheet = wb.createSheet("数据");

            //给单元格设置样式
            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();
            //设置字体大小
            font.setFontHeightInPoints((short) 12);
            //设置字体加粗
            font.setBold(true);
            //给字体设置样式
            cellStyle.setFont(font);
            //设置单元格背景颜色
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            //设置单元格填充样式(使用纯色背景颜色填充)
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            if (CollectionUtils.isNotEmpty(details)) {
                for (int i = 0; i < details.size(); i++) {
                    HSSFRow row = detailsSheet.createRow(i);
                    String[] rowData = details.get(i);
                    if (rowData != null) {
                        for (int j = 0; j < rowData.length; j++) {
                            HSSFCell cell = row.createCell(j);
                            if (i == 0) {// 头部
                                cell.setCellValue(rowData[j]);
                                cell.setCellStyle(cellStyle);
                                //设置列的宽度
                                detailsSheet.setColumnWidth(j, 255 * 20);
                            } else {
                                // with DataType
                                if ((excelTypes[j] == DeTypeConstants.DE_INT || excelTypes[j] == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData[j])) {
                                    try {
                                        cell.setCellValue(Double.valueOf(rowData[j]));
                                    } catch (Exception e) {
                                        LogUtil.warn("export excel data transform error");
                                    }
                                } else {
                                    cell.setCellValue(rowData[j]);
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtils.isNotEmpty(snapshot)) {
                //截图sheet 1px ≈ 2.33dx ≈ 0.48 dy  8*24 个单元格
                HSSFSheet snapshotSheet = wb.createSheet("图表");
                short reDefaultRowHeight = (short) Math.round(request.getSnapshotHeight() * 3.5 / 8);
                int reDefaultColumnWidth = (int) Math.round(request.getSnapshotWidth() * 0.25 / 24);
                snapshotSheet.setDefaultColumnWidth(reDefaultColumnWidth);
                snapshotSheet.setDefaultRowHeight(reDefaultRowHeight);

                //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）i
                HSSFPatriarch patriarch = snapshotSheet.createDrawingPatriarch();
                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, reDefaultColumnWidth, reDefaultColumnWidth, (short) 0, 0, (short) 8, 24);
                anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_DO_RESIZE);
                patriarch.createPicture(anchor, wb.addPicture(Base64Utils.decodeFromString(snapshot.replace(DATA_URL_TITLE, "")), HSSFWorkbook.PICTURE_TYPE_JPEG));
            }
            response.setContentType("application/vnd.ms-excel");
            //文件名称
            response.setHeader("Content-disposition", "attachment;filename=" + request.getViewName() + ".xls");
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        if (ObjectUtils.isNotEmpty(AuthUtils.getUser())) {
            String viewId = request.getViewId();
            ChartViewWithBLOBs chartViewWithBLOBs = chartViewService.get(viewId);
            String pid = chartViewWithBLOBs.getSceneId();
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.EXPORT, SysLogConstants.SOURCE_TYPE.VIEW, viewId, pid, null, null);
        }
    }

    public void updatePanelStatus(String panelId, PanelGroupBaseInfoRequest request) {
        Assert.notNull(request.getStatus(), "status can not be null");
        Assert.notNull(panelId, "panelId can not be null");
        PanelGroupWithBLOBs panelGroup = new PanelGroupWithBLOBs();
        panelGroup.setId(panelId);
        panelGroup.setStatus(request.getStatus());
        panelGroupMapper.updateByPrimaryKeySelective(panelGroup);
    }


    public PanelTemplateFileDTO getTemplateFromMarket(String templateUrl) {
        if (StringUtils.isNotEmpty(templateUrl)) {
            Gson gson = new Gson();
            String templateInfo = HttpClientUtil.get(templateUrl, null);
            return gson.fromJson(templateInfo, PanelTemplateFileDTO.class);
        } else {
            return null;
        }
    }

    /**
     * @Description: Automatically save panel data to cache when editing
     */
    public void autoCache(PanelGroupRequest request) {
        String cacheName = JdbcConstants.PANEL_CACHE_KEY + request.getId();
        String cacheId = AuthUtils.getUser().getUserId() + "&" + request.getId();
        CacheUtils.put(cacheName, cacheId, request, null, null);
    }

    /**
     * @Description: Remove panel cache for specific user
     */
    public void removePanelCache(String panelId) {
        String cacheName = JdbcConstants.PANEL_CACHE_KEY + panelId;
        String cacheId = AuthUtils.getUser().getUserId() + "&" + panelId;
        CacheUtils.remove(cacheName, cacheId);
    }

    public void removePanelAllCache(String panelId) {
        String cacheName = JdbcConstants.PANEL_CACHE_KEY + panelId;
        CacheUtils.removeAll(cacheName);
    }

    public PanelGroupDTO findUserPanelCache(String panelId) {
        String cacheName = JdbcConstants.PANEL_CACHE_KEY + panelId;
        String cacheId = AuthUtils.getUser().getUserId() + "&" + panelId;
        Object cache = CacheUtils.get(cacheName, cacheId);
        if (cache == null) {
            return null;
        } else {
            return (PanelGroupRequest) cache;
        }
    }

    public Boolean checkUserCache(String panelId) {
        String cacheName = JdbcConstants.PANEL_CACHE_KEY + panelId;
        String cacheId = AuthUtils.getUser().getUserId() + "&" + panelId;
        Object cache = CacheUtils.get(cacheName, cacheId);
        return cache != null;
    }

    public void viewLog(PanelViewLogRequest request) {
        String panelId = request.getPanelId();
        Boolean mobile = request.getMobile();
        PanelGroupWithBLOBs panel = panelGroupMapper.selectByPrimaryKey(panelId);
        SysLogConstants.OPERATE_TYPE operateType = SysLogConstants.OPERATE_TYPE.PC_VIEW;
        if (mobile) {
            operateType = SysLogConstants.OPERATE_TYPE.MB_VIEW;
        }
        DeLogUtils.save(operateType, sourceType, panelId, panel.getPid(), null, null);
    }

    public Object findPanelElementInfo(String viewId) {
        PanelView panelView = panelViewService.findByViewId(viewId);
        if (panelView != null) {
            PanelGroupWithBLOBs panelGroupWithBLOBs = panelGroupMapper.selectByPrimaryKey(panelView.getPanelId());
            if (panelGroupWithBLOBs != null) {
                JSONArray panelData = JSONObject.parseArray(panelGroupWithBLOBs.getPanelData());
                for (int i = 0; i < panelData.size(); i++) {
                    JSONObject element = panelData.getJSONObject(i);
                    if ("user-view".equals(element.getString("component"))) {
                        return element;
                    }
                }
            }
        }
        return null;
    }

    public PanelExport2App panelExport2AppCheck(String panelId) {
        //1.获取所有视图信息
        List<ChartViewWithBLOBs> chartViewsInfo = panelViewService.findByPanelId(panelId);
        //2.获取视图扩展字段信息
        List<ChartViewField> chartViewFieldsInfo = extChartViewFieldMapper.findByPanelId(panelId);
        //3.获取所有数据集信息
        List<DatasetTable> datasetTablesInfo = extDataSetTableMapper.findByPanelId(panelId);
        //4.获取所有数据集字段信息
        List<DatasetTableField> datasetTableFieldsInfo = extDataSetTableFieldMapper.findByPanelId(panelId);
        //5.获取所有任务信息
        List<DataSetTaskDTO> dataSetTasksInfo = extDataSetTaskMapper.findByPanelId(panelId);
        //6.获取所有数据源信息
        List<DatasourceDTO> datasourceDTOS = extDataSourceMapper.findByPanelId(panelId);

        List<PanelView> panelViews = panelViewService.findPanelViewsByPanelId(panelId);

        //校验标准 1.存在视图且所有视图的数据来源必须是dataset 2.存在数据集且没有excel数据集 3.存在数据源且是单数据源
        //1.view check
        if (CollectionUtils.isEmpty(chartViewsInfo)) {
            return new PanelExport2App("this panel don't have views");
        } else if (chartViewsInfo.stream().filter(chartView -> chartView.getDataFrom().equals("template")).collect(Collectors.toList()).size() > 0) {
            return new PanelExport2App("this panel have view from template");
        }

        // dataset check
        if (CollectionUtils.isEmpty(datasetTablesInfo)) {
            return new PanelExport2App("this panel don't have dataset");
        } else if (datasetTablesInfo.stream().filter(datasetTable -> datasetTable.getType().equals("excel")).collect(Collectors.toList()).size() > 0) {
            return new PanelExport2App("this panel have dataset witch type is excel");
        }

        //datasource check
        if (CollectionUtils.isEmpty(datasourceDTOS)) {
            return new PanelExport2App("this panel don't have datasource");
        } else if (datasourceDTOS.size() > 1) {
            return new PanelExport2App("this panel should hava only one dataset");
        }
        return new PanelExport2App(chartViewsInfo, chartViewFieldsInfo, datasetTablesInfo, datasetTableFieldsInfo, dataSetTasksInfo, datasourceDTOS,panelViews);
    }

    @Transactional(rollbackFor = Exception.class)
    public String appApply(PanelAppTemplateApplyRequest request) throws Exception{
        //仪表板名称校验，数据集分组名称校验，数据源名称校验
        panelAppTemplateService.nameCheck(request);

        String newPanelId = UUIDUtil.getUUIDAsString();
        // 新建数据集分组
        DatasetGroup newDatasetGroup = new DatasetGroup();
        newDatasetGroup.setPid(request.getDatasetGroupId());
        newDatasetGroup.setName(request.getDatasetGroupName());
        newDatasetGroup.setType("group");
        DataSetGroupDTO resultDatasetGroup = dataSetGroupService.save(newDatasetGroup);

        String asideDatasetGroupId = resultDatasetGroup.getId();
        //查询应用信息
        PanelAppTemplateWithBLOBs appInfo = panelAppTemplateMapper.selectByPrimaryKey(request.getAppTemplateId());
        //1.获取所有视图信息
        List<ChartViewWithBLOBs> chartViewsInfo = gson.fromJson(appInfo.getChartViewsInfo(), new TypeToken<List<ChartViewWithBLOBs>>(){}.getType());
        //2.获取视图扩展字段信息
        List<ChartViewField> chartViewFieldsInfo = gson.fromJson(appInfo.getChartViewFieldsInfo(), new TypeToken<List<ChartViewField>>(){}.getType());
        //3.获取所有数据集信息
        List<DatasetTable> datasetTablesInfo  = gson.fromJson(appInfo.getDatasetTablesInfo(), new TypeToken<List<DatasetTable>>(){}.getType());
        //4.获取所有数据集字段信息
        List<DatasetTableField> datasetTableFieldsInfo = gson.fromJson(appInfo.getDatasetTableFieldsInfo(), new TypeToken<List<DatasetTableField>>(){}.getType());
        //5.获取所有任务信息
        List<DataSetTaskDTO> dataSetTasksInfo = gson.fromJson(appInfo.getDatasetTasksInfo(), new TypeToken<List<DataSetTaskDTO>>(){}.getType());
        //6.获取所有数据源信息
        List<Datasource> oldDatasourceInfo = gson.fromJson(appInfo.getDatasourceInfo(), new TypeToken<List<Datasource>>(){}.getType());
        //获取仪表板信息
        PanelGroupRequest  panelInfo = gson.fromJson(appInfo.getPanelInfo(),PanelGroupRequest.class);
        //获取仪表板视图信息
        List<PanelView> panelViewsInfo = gson.fromJson(appInfo.getPanelViewsInfo(), new TypeToken<List<PanelView>>(){}.getType());

        Map<String,String> datasourceRelaMap = panelAppTemplateService.applyDatasource(oldDatasourceInfo,request.getDatasourceList());

        Map<String,String> datasetsRelaMap = panelAppTemplateService.applyDataset(datasetTablesInfo,datasourceRelaMap,asideDatasetGroupId);

        Map<String,String> datasetFieldsRelaMap = panelAppTemplateService.applyDatasetField(datasetTableFieldsInfo,datasetsRelaMap);

        panelAppTemplateService.resetCustomAndUnionDataset(datasetTablesInfo,datasetsRelaMap,datasetFieldsRelaMap);

        Map<String,String> chartViewsRelaMap = panelAppTemplateService.applyViews(chartViewsInfo,datasetsRelaMap,datasetFieldsRelaMap,newPanelId);

        panelAppTemplateService.applyViewsField(chartViewFieldsInfo,chartViewsRelaMap,datasetsRelaMap,datasetFieldsRelaMap);

        panelAppTemplateService.applyPanel(panelInfo,chartViewsRelaMap,newPanelId, request.getPanelName(), request.getPanelId());

        panelAppTemplateService.applyPanelView(panelViewsInfo,chartViewsRelaMap,newPanelId);

        String newDatasourceId =datasourceRelaMap.entrySet().stream().findFirst().get().getValue();

        String newDatasourceName = request.getDatasourceList().get(0).getName();

        PanelAppTemplateLog templateLog = new PanelAppTemplateLog();
        templateLog.setPanelId(newPanelId);
        templateLog.setSourcePanelName(request.getPanelName());
        templateLog.setDatasourceId(newDatasourceId);
        templateLog.setSourceDatasourceName(newDatasourceName);
        templateLog.setDatasetGroupId(asideDatasetGroupId);
        templateLog.setSourceDatasetGroupName(request.getDatasetGroupName());
        templateLog.setAppTemplateId(appInfo.getId());
        templateLog.setAppTemplateName(appInfo.getName());
        appTemplateLogService.newAppApplyLog(templateLog);
        return newPanelId;
    }
}
