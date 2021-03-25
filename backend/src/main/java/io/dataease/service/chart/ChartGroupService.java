package io.dataease.service.chart;

import io.dataease.base.domain.ChartGroup;
import io.dataease.base.domain.ChartGroupExample;
import io.dataease.base.domain.DatasetGroup;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.mapper.ChartGroupMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.chart.ChartGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.chart.ChartGroupDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ChartGroupService {
    @Resource
    private ChartGroupMapper chartGroupMapper;
    @Resource
    private ChartViewService chartViewService;

    public ChartGroupDTO save(ChartGroup chartGroup) {
        if (StringUtils.isEmpty(chartGroup.getId())) {
            chartGroup.setId(UUID.randomUUID().toString());
            chartGroup.setCreateTime(System.currentTimeMillis());
            chartGroupMapper.insert(chartGroup);
        } else {
            chartGroupMapper.updateByPrimaryKey(chartGroup);
        }
        ChartGroupDTO ChartGroupDTO = new ChartGroupDTO();
        BeanUtils.copyBean(ChartGroupDTO, chartGroup);
        ChartGroupDTO.setLabel(ChartGroupDTO.getName());
        return ChartGroupDTO;
    }

    public void delete(String id) {
        ChartGroupRequest ChartGroup = new ChartGroupRequest();
        ChartGroup.setId(id);
        List<ChartGroupDTO> tree = tree(ChartGroup);
        List<String> ids = new ArrayList<>();
        getAllId(tree, ids);
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

    public List<ChartGroupDTO> tree(ChartGroupRequest ChartGroup) {
        ChartGroupExample ChartGroupExample = new ChartGroupExample();
        ChartGroupExample.Criteria criteria = ChartGroupExample.createCriteria();
        if (StringUtils.isNotEmpty(ChartGroup.getName())) {
            criteria.andNameLike("%" + ChartGroup.getName() + "%");
        }
        if (StringUtils.isNotEmpty(ChartGroup.getType())) {
            criteria.andTypeEqualTo(ChartGroup.getType());
        }
        if (StringUtils.isNotEmpty(ChartGroup.getId())) {
            criteria.andIdEqualTo(ChartGroup.getId());
        } else {
            criteria.andLevelEqualTo(0);
        }
        ChartGroupExample.setOrderByClause(ChartGroup.getSort());
        List<ChartGroup> ChartGroups = chartGroupMapper.selectByExample(ChartGroupExample);
        List<ChartGroupDTO> DTOs = ChartGroups.stream().map(ele -> {
            ChartGroupDTO dto = new ChartGroupDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setLabel(ele.getName());
            return dto;
        }).collect(Collectors.toList());
        getAll(DTOs, ChartGroup);
        return DTOs;
    }

    public void getAll(List<ChartGroupDTO> list, ChartGroupRequest ChartGroup) {
        for (ChartGroupDTO obj : list) {
            ChartGroupExample ChartGroupExample = new ChartGroupExample();
            ChartGroupExample.Criteria criteria = ChartGroupExample.createCriteria();
            if (StringUtils.isNotEmpty(ChartGroup.getName())) {
                criteria.andNameLike("%" + ChartGroup.getName() + "%");
            }
            if (StringUtils.isNotEmpty(ChartGroup.getType())) {
                criteria.andTypeEqualTo(ChartGroup.getType());
            }
            criteria.andPidEqualTo(obj.getId());
            ChartGroupExample.setOrderByClause(ChartGroup.getSort());
            List<ChartGroup> ChartGroups = chartGroupMapper.selectByExample(ChartGroupExample);
            List<ChartGroupDTO> DTOs = ChartGroups.stream().map(ele -> {
                ChartGroupDTO dto = new ChartGroupDTO();
                BeanUtils.copyBean(dto, ele);
                dto.setLabel(ele.getName());
                return dto;
            }).collect(Collectors.toList());
            obj.setChildren(DTOs);
            if (CollectionUtils.isNotEmpty(DTOs)) {
                getAll(DTOs, ChartGroup);
            }
        }
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
}
