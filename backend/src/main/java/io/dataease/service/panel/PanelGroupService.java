package io.dataease.service.panel;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.base.mapper.PanelDesignMapper;
import io.dataease.base.mapper.PanelGroupMapper;
import io.dataease.base.mapper.ext.ExtPanelGroupMapper;
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
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<PanelGroupDTO> tree(PanelGroupRequest panelGroupRequest) {
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(panelGroupRequest);
        getTreeChildren(panelGroupDTOList);
        return panelGroupDTOList;
    }

    public void getTreeChildren(List<PanelGroupDTO> parentPanelGroupDTO){
        Optional.ofNullable(parentPanelGroupDTO).ifPresent(parent -> parent.forEach(panelGroupDTO -> {
            List<PanelGroupDTO> panelGroupDTOChildren = extPanelGroupMapper.panelGroupList(new PanelGroupRequest(panelGroupDTO.getId()));
            panelGroupDTO.setChildren(panelGroupDTOChildren);
            getTreeChildren(panelGroupDTOChildren);
        }));
    }

    public List<PanelGroupDTO> getDefaultTree(PanelGroupRequest panelGroupRequest){
        return extPanelGroupMapper.panelGroupList(panelGroupRequest);
    }


    public PanelGroupDTO save(PanelGroupRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            panelGroupMapper.insert(request);
        } else {
            panelGroupMapper.updateByPrimaryKey(request);
        }
        PanelGroupDTO panelGroupDTO = new PanelGroupDTO();
        BeanUtils.copyBean(panelGroupDTO, request);
        panelGroupDTO.setLabel(request.getName());
        return panelGroupDTO;
    }


    public void deleteCircle(String id){
        Assert.notNull(id, "id cannot be null");
        extPanelGroupMapper.deleteCircle(id);
    }


    public PanelGroupDTO findOne(String panelId) throws Exception{
        PanelGroupDTO panelGroupDTO = extPanelGroupMapper.panelGroup(panelId);
        Assert.notNull(panelGroupDTO, "未查询到仪表盘信息");
        PanelDesignExample panelDesignExample = new PanelDesignExample();
        panelDesignExample.createCriteria().andPanelIdEqualTo(panelId);
        List<PanelDesign>  panelDesignList = panelDesignMapper.selectByExample(panelDesignExample);
        if(CollectionUtils.isNotEmpty(panelDesignList)){
            List<PanelDesignDTO> panelDesignDTOList = new ArrayList<>();
            //TODO 加载所有视图和组件的数据
            for(PanelDesign panelDesign:panelDesignList){
                //TODO 获取view 视图数据
                ChartViewDTO chartViewDTO = chartViewService.getData(panelDesign.getComponentId());
                //TODO 获取systemComponent 系统组件数据（待开发）

                PanelDesignDTO panelDesignDTO = new PanelDesignDTO(chartViewDTO);
                BeanUtils.copyBean(panelDesignDTO,panelDesign);
                panelDesignDTO.setKeepFlag(true);
                panelDesignDTOList.add(panelDesignDTO);
            }
            panelGroupDTO.setPanelDesigns(panelDesignDTOList);
        }
        //获取所有可用的视图
        panelGroupDTO.setViewsUsable(getUsableViews(panelId));
        return panelGroupDTO;
    }


    public List<ChartViewDTO> getUsableViews(String panelId) throws Exception{
        List<ChartViewDTO> chartViewDTOList = new ArrayList<>();
        List<ChartView> allChartView = chartViewMapper.selectByExample(null);
        Optional.ofNullable(allChartView).orElse(new ArrayList<>()).stream().forEach(chartView -> {
            try {
                chartViewDTOList.add(chartViewService.getData(chartView.getId()));
            }catch (Exception e){
                LOGGER.error("获取view详情出错："+chartView.getId(),e);
            }
        });
        return chartViewDTOList;
    }

}
