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
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.panel.PanelDesignDTO;
import io.dataease.dto.panel.PanelGroupDTO;
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
        getTreeChildren(panelGroupDTOList,userId);
        return panelGroupDTOList;
    }

    public void getTreeChildren(List<PanelGroupDTO> parentPanelGroupDTO,String userId) {
        Optional.ofNullable(parentPanelGroupDTO).ifPresent(parent -> parent.forEach(panelGroupDTO -> {
            List<PanelGroupDTO> panelGroupDTOChildren = extPanelGroupMapper.panelGroupList(new PanelGroupRequest(panelGroupDTO.getId(),userId));
            panelGroupDTO.setChildren(panelGroupDTOChildren);
            getTreeChildren(panelGroupDTOChildren,userId);
        }));
    }

    public List<PanelGroupDTO> getDefaultTree(PanelGroupRequest panelGroupRequest) {
        return extPanelGroupMapper.panelGroupListDefault(panelGroupRequest);
    }


    public PanelGroupDTO save(PanelGroupRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setCreateBy(AuthUtils.getUser().getUsername());
            panelGroupMapper.insert(request);
        } else {
            // 复制为默认仪表盘
            if("toDefaultPanel".equals(request.getOptType())){
                PanelGroupWithBLOBs newDefaultPanel =  panelGroupMapper.selectByPrimaryKey(request.getId());
                newDefaultPanel.setPanelType(PanelConstants.PANEL_TYPE_SYSTEM);
                newDefaultPanel.setNodeType(PanelConstants.PANEL_NODE_TYPE_PANEL);
                newDefaultPanel.setName(request.getName());
                newDefaultPanel.setId(UUID.randomUUID().toString());
                newDefaultPanel.setPid(null);
                newDefaultPanel.setLevel(0);
                panelGroupMapper.insertSelective(newDefaultPanel);
            }else{
                panelGroupMapper.updateByPrimaryKeySelective(request);
            }
        }
        PanelGroupDTO panelGroupDTO = new PanelGroupDTO();
        BeanUtils.copyBean(panelGroupDTO, request);
        panelGroupDTO.setLabel(request.getName());
        return panelGroupDTO;
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
