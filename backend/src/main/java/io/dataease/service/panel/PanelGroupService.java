package io.dataease.service.panel;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.base.mapper.PanelDesignMapper;
import io.dataease.base.mapper.PanelGroupMapper;
import io.dataease.base.mapper.ext.ExtPanelDesignMapper;
import io.dataease.base.mapper.ext.ExtPanelGroupMapper;
import io.dataease.commons.constants.PanelConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.panel.PanelDesignDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.i18n.Translator;
import io.dataease.service.chart.ChartViewService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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

    public List<PanelGroupDTO> tree(PanelGroupRequest panelGroupRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        panelGroupRequest.setUserId(userId);
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(panelGroupRequest);
        List<PanelGroupDTO> result = TreeUtils.mergeTree(panelGroupDTOList,"panel_list");
        return result;
    }

    public List<PanelGroupDTO> defaultTree(PanelGroupRequest panelGroupRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        panelGroupRequest.setUserId(userId);
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(panelGroupRequest);
        List<PanelGroupDTO> result = TreeUtils.mergeTree(panelGroupDTOList,"default_panel");
        return result;
    }

    public PanelGroup saveOrUpdate(PanelGroupRequest request) {
        String panelId = request.getId();
        if (StringUtils.isEmpty(panelId)) {
            // 新建
            checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_INSERT, null);
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
            checkPanelName(newDefaultPanel.getName(), newDefaultPanel.getPid(), PanelConstants.OPT_TYPE_INSERT, newDefaultPanel.getId());
            panelGroupMapper.insertSelective(newDefaultPanel);
        } else {
            // 更新
            if (StringUtils.isNotEmpty(request.getName())) {
                checkPanelName(request.getName(), request.getPid(), PanelConstants.OPT_TYPE_UPDATE, request.getId());
            }
            panelGroupMapper.updateByPrimaryKeySelective(request);
        }

        //带有权限的返回
        PanelGroupRequest authRequest = new PanelGroupRequest();
        authRequest.setId(panelId);
        authRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(authRequest);

        return panelGroupDTOList.get(0);
    }


    private void checkPanelName(String name, String pid, String optType, String id) {
        PanelGroupExample groupExample = new PanelGroupExample();
        if (PanelConstants.OPT_TYPE_INSERT.equalsIgnoreCase(optType)) {
            groupExample.createCriteria().andPidEqualTo(pid).andNameEqualTo(name);
        } else if (PanelConstants.OPT_TYPE_UPDATE.equalsIgnoreCase(optType)) {
            groupExample.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id);
        }

        List<PanelGroup> checkResult = panelGroupMapper.selectByExample(groupExample);
        if (CollectionUtils.isNotEmpty(checkResult)) {
            throw new RuntimeException(Translator.get("i18n_same_folder_can_not_repeat"));
        }
    }


    public void deleteCircle(String id) {
        Assert.notNull(id, "id cannot be null");
        extPanelGroupMapper.deleteCircle(id);
        storeService.removeByPanelId(id);
        shareService.delete(id, null);
        panelLinkService.deleteByResourceId(id);
    }


    public PanelGroupWithBLOBs findOne(String panelId) {
        return panelGroupMapper.selectByPrimaryKey(panelId);
    }


    public List<ChartViewDTO> getUsableViews(String panelId) throws Exception {
        List<ChartViewDTO> chartViewDTOList = new ArrayList<>();
        List<ChartView> allChartView = chartViewMapper.selectByExample(null);
        Optional.ofNullable(allChartView).orElse(new ArrayList<>()).stream().forEach(chartView -> {
            try {
                chartViewDTOList.add(chartViewService.getData(chartView.getId(), null));
            } catch (Exception e) {
                LOGGER.error("获取view详情出错：" + chartView.getId(), e);
            }
        });
        return chartViewDTOList;
    }
}
