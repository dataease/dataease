package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.base.domain.DatasetGroupExample;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.mapper.DatasetGroupMapper;
import io.dataease.base.mapper.ext.ExtDataSetGroupMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
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
    @Lazy
    private DataSetTableService dataSetTableService;
    @Resource
    private ExtDataSetGroupMapper extDataSetGroupMapper;

    public DataSetGroupDTO save(DatasetGroup datasetGroup) {
        checkName(datasetGroup);
        if (StringUtils.isEmpty(datasetGroup.getId())) {
            datasetGroup.setId(UUID.randomUUID().toString());
            datasetGroup.setCreateBy(AuthUtils.getUser().getUsername());
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

    public void delete(String id) throws Exception {
        DatasetGroup dg = datasetGroupMapper.selectByPrimaryKey(id);
        DataSetGroupRequest datasetGroup = new DataSetGroupRequest();
        BeanUtils.copyBean(datasetGroup, dg);
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

    public void deleteTableAndField(List<String> sceneIds) throws Exception {
        for (String sceneId : sceneIds) {
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            dataSetTableRequest.setSceneId(sceneId);
            List<DataSetTableDTO> list = dataSetTableService.list(dataSetTableRequest);
            for (DataSetTableDTO table : list) {
                dataSetTableService.delete(table.getId());
            }
        }
    }

    public List<DataSetGroupDTO> tree(DataSetGroupRequest datasetGroup) {
        datasetGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<DataSetGroupDTO> treeInfo = extDataSetGroupMapper.search(datasetGroup);
        List<DataSetGroupDTO> result = TreeUtils.mergeTree(treeInfo);
        return result;
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

    private void checkName(DatasetGroup datasetGroup) {
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        DatasetGroupExample.Criteria criteria = datasetGroupExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetGroup.getPid())) {
            criteria.andPidEqualTo(datasetGroup.getPid());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getType())) {
            criteria.andTypeEqualTo(datasetGroup.getType());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getName())) {
            criteria.andNameEqualTo(datasetGroup.getName());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getId())) {
            criteria.andIdNotEqualTo(datasetGroup.getId());
        }
        List<DatasetGroup> list = datasetGroupMapper.selectByExample(datasetGroupExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
    }

    public List<DatasetGroup> getParents(String id) {
        List<DatasetGroup> list = new ArrayList<>();
        DatasetGroup datasetGroup = datasetGroupMapper.selectByPrimaryKey(id);
        list.add(datasetGroup);
        getParent(list, datasetGroup);
        Collections.reverse(list);
        return list;
    }

    public void getParent(List<DatasetGroup> list, DatasetGroup datasetGroup) {
        if (StringUtils.isNotEmpty(datasetGroup.getPid())) {
            DatasetGroup d = datasetGroupMapper.selectByPrimaryKey(datasetGroup.getPid());
            list.add(d);
            getParent(list, d);
        }
    }
}
