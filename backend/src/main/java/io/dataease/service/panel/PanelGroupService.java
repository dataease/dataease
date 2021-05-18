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
    private PanelDesignMapper panelDesignMapper;
    @Resource
    private ChartViewService chartViewService;
    @Resource
    private ChartViewMapper chartViewMapper;
    @Resource
    private ExtPanelDesignMapper extPanelDesignMapper;

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
        return extPanelGroupMapper.panelGroupList(panelGroupRequest);
    }


    public PanelGroupDTO save(PanelGroupRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setCreateBy(AuthUtils.getUser().getUsername());
            panelGroupMapper.insert(request);
        } else {
            panelGroupMapper.updateByPrimaryKeySelective(request);
        }
        PanelGroupDTO panelGroupDTO = new PanelGroupDTO();
        BeanUtils.copyBean(panelGroupDTO, request);
        panelGroupDTO.setLabel(request.getName());
        return panelGroupDTO;
    }


    public void deleteCircle(String id) {
        Assert.notNull(id, "id cannot be null");
        extPanelGroupMapper.deleteCircle(id);
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
