package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.base.domain.DatasetGroupExample;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.mapper.DatasetGroupMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/20 8:10 下午
 */
@Service
public class DataSetGroupService {
    @Resource
    private DatasetGroupMapper datasetGroupMapper;
    @Resource
    private DataSetTableService dataSetTableService;

    public DataSetGroupDTO save(DatasetGroup datasetGroup) {
        if (StringUtils.isEmpty(datasetGroup.getId())) {
            datasetGroup.setId(UUID.randomUUID().toString());
            datasetGroup.setCreateTime(System.currentTimeMillis());
            datasetGroupMapper.insert(datasetGroup);
        } else {
            datasetGroupMapper.updateByPrimaryKeySelective(datasetGroup);
        }
        DataSetGroupDTO dataSetGroupDTO = new DataSetGroupDTO();
        BeanUtils.copyBean(dataSetGroupDTO, datasetGroup);
        dataSetGroupDTO.setLabel(dataSetGroupDTO.getName());
        return dataSetGroupDTO;
    }

    public void delete(String id) {
        DataSetGroupRequest datasetGroup = new DataSetGroupRequest();
        datasetGroup.setId(id);
        List<DataSetGroupDTO> tree = tree(datasetGroup);
        List<String> ids = new ArrayList<>();
        getAllId(tree, ids);
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        datasetGroupExample.createCriteria().andIdIn(ids);
        datasetGroupMapper.deleteByExample(datasetGroupExample);
        // 删除场景下的表和字段
        deleteTableAndField(ids);
    }

    public DatasetGroup getScene(String id) {
        return datasetGroupMapper.selectByPrimaryKey(id);
    }

    public void deleteTableAndField(List<String> sceneIds) {
        for (String sceneId : sceneIds) {
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            dataSetTableRequest.setSceneId(sceneId);
            List<DatasetTable> list = dataSetTableService.list(dataSetTableRequest);
            for (DatasetTable table : list) {
                dataSetTableService.delete(table.getId());
            }
        }
    }

    public List<DataSetGroupDTO> tree(DataSetGroupRequest datasetGroup) {
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        DatasetGroupExample.Criteria criteria = datasetGroupExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetGroup.getName())) {
            criteria.andNameLike("%" + datasetGroup.getName() + "%");
        }
        if (StringUtils.isNotEmpty(datasetGroup.getType())) {
            criteria.andTypeEqualTo(datasetGroup.getType());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getId())) {
            criteria.andIdEqualTo(datasetGroup.getId());
        } else {
            criteria.andLevelEqualTo(0);
        }
        datasetGroupExample.setOrderByClause(datasetGroup.getSort());
        List<DatasetGroup> datasetGroups = datasetGroupMapper.selectByExample(datasetGroupExample);
        List<DataSetGroupDTO> DTOs = datasetGroups.stream().map(ele -> {
            DataSetGroupDTO dto = new DataSetGroupDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setLabel(ele.getName());
            return dto;
        }).collect(Collectors.toList());
        getAll(DTOs, datasetGroup);
        return DTOs;
    }

    public void getAll(List<DataSetGroupDTO> list, DataSetGroupRequest datasetGroup) {
        for (DataSetGroupDTO obj : list) {
            DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
            DatasetGroupExample.Criteria criteria = datasetGroupExample.createCriteria();
            if (StringUtils.isNotEmpty(datasetGroup.getName())) {
                criteria.andNameLike("%" + datasetGroup.getName() + "%");
            }
            if (StringUtils.isNotEmpty(datasetGroup.getType())) {
                criteria.andTypeEqualTo(datasetGroup.getType());
            }
            criteria.andPidEqualTo(obj.getId());
            datasetGroupExample.setOrderByClause(datasetGroup.getSort());
            List<DatasetGroup> datasetGroups = datasetGroupMapper.selectByExample(datasetGroupExample);
            List<DataSetGroupDTO> DTOs = datasetGroups.stream().map(ele -> {
                DataSetGroupDTO dto = new DataSetGroupDTO();
                BeanUtils.copyBean(dto, ele);
                dto.setLabel(ele.getName());
                return dto;
            }).collect(Collectors.toList());
            obj.setChildren(DTOs);
            if (CollectionUtils.isNotEmpty(DTOs)) {
                getAll(DTOs, datasetGroup);
            }
        }
    }

    public List<String> getAllId(List<DataSetGroupDTO> list, List<String> ids) {
        for (DataSetGroupDTO dto : list) {
            ids.add(dto.getId());
            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                getAllId(dto.getChildren(), ids);
            }
        }
        return ids;
    }
}
