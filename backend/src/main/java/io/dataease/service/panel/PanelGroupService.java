package io.dataease.service.panel;

import com.alibaba.fastjson.JSON;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.base.mapper.PanelGroupMapper;
import io.dataease.base.mapper.PanelViewMapper;
import io.dataease.base.mapper.VAuthModelMapper;
import io.dataease.base.mapper.ext.*;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.PanelConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.dto.panel.linkJump.PanelLinkJumpBaseRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.sys.SysAuthService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
//    @Transactional
    public PanelGroup saveOrUpdate(PanelGroupRequest request) {
        try {
            Boolean mobileLayout = panelViewService.syncPanelViews(request);
            request.setMobileLayout(mobileLayout);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("更新panelView出错panelId：{}", request.getId());
        }
        String panelId = request.getId();
        if (StringUtils.isEmpty(panelId)) {
            // 新建
            checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_INSERT, null, request.getNodeType());
            panelId = UUID.randomUUID().toString();
            request.setId(panelId);
            request.setCreateTime(System.currentTimeMillis());
            request.setCreateBy(AuthUtils.getUser().getUsername());
            panelGroupMapper.insert(request);
        } else if ("toDefaultPanel".equals(request.getOptType())) {
            panelId = UUID.randomUUID().toString();
            // 转存为默认仪表板
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
        } else if ("copy".equals(request.getOptType())) {
            panelId = this.panelGroupCopy(request, null, true);
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

        //移除没有用到的仪表板私有视图
        extPanelGroupMapper.removeUselessViews(panelId);
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

}
