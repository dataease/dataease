package io.dataease.service.chart;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartGroupMapper;
import io.dataease.base.mapper.ext.ExtChartGroupMapper;
import io.dataease.base.mapper.ext.ExtDataSetGroupMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.chart.ChartGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.chart.ChartGroupDTO;
import io.dataease.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ChartGroupService {
    @Resource
    private ChartGroupMapper chartGroupMapper;
    @Resource
    private ChartViewService chartViewService;
    @Resource
    private ExtChartGroupMapper extChartGroupMapper;
    @Resource
    private ExtDataSetGroupMapper extDataSetGroupMapper;

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

    public void delete(String id) {
        ChartGroup cg = chartGroupMapper.selectByPrimaryKey(id);
        ChartGroupRequest ChartGroup = new ChartGroupRequest();
        BeanUtils.copyBean(ChartGroup, cg);
        Map<String, String> stringStringMap = extDataSetGroupMapper.searchIds(id, "chart");
        String[] split = stringStringMap.get("ids").split(",");
        List<String> ids = new ArrayList<>();
        for (String dsId : split) {
            if (StringUtils.isNotEmpty(dsId)) {
                ids.add(dsId);
            }
        }
        ChartGroupExample ChartGroupExample = new ChartGroupExample();
        ChartGroupExample.createCriteria().andIdIn(ids);
        chartGroupMapper.deleteByExample(ChartGroupExample);
        // 删除所有chart
        deleteChart(ids);
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
        chartGroup.setPid("0");
        chartGroup.setType("group");
        chartGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<ChartGroupDTO> treeInfo = extChartGroupMapper.search(chartGroup);
        List<ChartGroupDTO> result = TreeUtils.mergeTree(treeInfo);
        return result;
    }

    public List<ChartGroupDTO> treeNode(ChartGroupRequest chartGroup) {
        chartGroup.setLevel(null);
        chartGroup.setPid(null);
        chartGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<ChartGroupDTO> treeInfo = extChartGroupMapper.search(chartGroup);
        List<ChartGroupDTO> result = TreeUtils.mergeTree(treeInfo);
        return result;
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
        List<ChartGroup> list = chartGroupMapper.selectByExample(chartGroupExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
    }
}
