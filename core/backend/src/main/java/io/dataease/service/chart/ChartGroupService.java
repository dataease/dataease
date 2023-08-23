package io.dataease.service.chart;

import io.dataease.ext.*;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.chart.ChartGroupRequest;
import io.dataease.dto.chart.ChartGroupDTO;
import io.dataease.plugins.common.base.domain.ChartGroup;
import io.dataease.plugins.common.base.domain.ChartGroupExample;
import io.dataease.plugins.common.base.mapper.ChartGroupMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;


@Service
public class ChartGroupService {
    @Resource
    private ChartGroupMapper chartGroupMapper;
    @Resource
    private ChartViewService chartViewService;
    @Resource
    private ExtChartGroupMapper extChartGroupMapper;
    @Resource
    private ExtChartViewMapper extChartViewMapper;

    public ChartGroupDTO save(ChartGroup chartGroup) {
        checkName(chartGroup);
        if (StringUtils.isEmpty(chartGroup.getId())) {
            chartGroup.setId(UUID.randomUUID().toString());
            chartGroup.setCreateBy(AuthUtils.getUser().getUsername());
            chartGroup.setCreateTime(System.currentTimeMillis());
            chartGroupMapper.insert(chartGroup);
        } else {
            chartGroupMapper.updateByPrimaryKeySelective(chartGroup);
        }
        ChartGroupDTO ChartGroupDTO = new ChartGroupDTO();
        BeanUtils.copyBean(ChartGroupDTO, chartGroup);
        ChartGroupDTO.setLabel(ChartGroupDTO.getName());
        return ChartGroupDTO;
    }

    @Transactional
    public void deleteCircle(String id) {
        Assert.notNull(id, "id cannot be null");
        //存量视图删除
        extChartViewMapper.deleteCircleView(id);
        //存量分组删除
        extChartViewMapper.deleteCircleGroup(id);
    }

    public void deleteChart(List<String> sceneIds) {
        for (String sceneId : sceneIds) {
            chartViewService.deleteBySceneId(sceneId);
        }
    }

    public ChartGroup getScene(String id) {
        return chartGroupMapper.selectByPrimaryKey(id);
    }

    public List<ChartGroupDTO> tree(ChartGroupRequest chartGroup) {
        chartGroup.setLevel(null);
        chartGroup.setPid(null);
        chartGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<ChartGroupDTO> treeInfo = extChartGroupMapper.search(chartGroup);
        return TreeUtils.mergeTree(treeInfo);
    }

    public List<ChartGroupDTO> treeNode(ChartGroupRequest chartGroup) {
        chartGroup.setLevel(null);
        chartGroup.setPid("0");
        chartGroup.setType("group");
        chartGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<ChartGroupDTO> treeInfo = extChartGroupMapper.search(chartGroup);
        return TreeUtils.mergeTree(treeInfo);
    }

    public List<String> getAllId(List<ChartGroupDTO> list, List<String> ids) {
        for (ChartGroupDTO dto : list) {
            ids.add(dto.getId());
            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                getAllId(dto.getChildren(), ids);
            }
        }
        return ids;
    }

    private void checkName(ChartGroup chartGroup) {
        ChartGroupExample chartGroupExample = new ChartGroupExample();
        ChartGroupExample.Criteria criteria = chartGroupExample.createCriteria();
        if (StringUtils.isNotEmpty(chartGroup.getPid())) {
            criteria.andPidEqualTo(chartGroup.getPid());
        }
        if (StringUtils.isNotEmpty(chartGroup.getType())) {
            criteria.andTypeEqualTo(chartGroup.getType());
        }
        if (StringUtils.isNotEmpty(chartGroup.getName())) {
            criteria.andNameEqualTo(chartGroup.getName());
        }
        if (StringUtils.isNotEmpty(chartGroup.getId())) {
            criteria.andIdNotEqualTo(chartGroup.getId());
        }
        if (ObjectUtils.isNotEmpty(chartGroup.getLevel())) {
            criteria.andLevelEqualTo(chartGroup.getLevel());
        }
    }
}
