package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.vo.DatasetTreeNodeVO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class DatasetGroupManage {
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;

    public DatasetNodeDTO save(DatasetNodeDTO datasetNodeDTO) {
        checkName(datasetNodeDTO);
        // todo node_type=dataset需要创建dataset_table和field
        if (StringUtils.equalsIgnoreCase(datasetNodeDTO.getNodeType(), "dataset")) {
            // todo coding...
        }
        // save dataset group
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        if (StringUtils.isEmpty(datasetNodeDTO.getId())) {
            datasetNodeDTO.setId(UUID.randomUUID().toString());
            datasetNodeDTO.setCreateBy("admin");// todo username
            datasetNodeDTO.setCreateTime(System.currentTimeMillis());
            BeanUtils.copyBean(coreDatasetGroup, datasetNodeDTO);
            coreDatasetGroupMapper.insert(coreDatasetGroup);
        } else {
            BeanUtils.copyBean(coreDatasetGroup, datasetNodeDTO);
            coreDatasetGroupMapper.updateById(coreDatasetGroup);
        }
        return datasetNodeDTO;
    }

    public void delete(String id) {
        coreDatasetGroupMapper.deleteById(id);
    }

    public List<DatasetTreeNodeVO> tree(DatasetNodeDTO datasetNodeDTO) {
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        List<CoreDatasetGroup> coreDatasetTables = coreDatasetGroupMapper.selectList(wrapper);
        List<DatasetTreeNodeVO> collect = coreDatasetTables.stream().map(ele -> {
            DatasetTreeNodeVO vo = new DatasetTreeNodeVO();
            BeanUtils.copyBean(vo, ele);
            return vo;
        }).collect(Collectors.toList());
        return TreeUtils.mergeTree(collect);
    }

    private void checkName(DatasetNodeDTO datasetNodeDTO) {
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(datasetNodeDTO.getPid())) {
            wrapper.eq("pid", datasetNodeDTO.getPid());
        }
        if (StringUtils.isNotEmpty(datasetNodeDTO.getName())) {
            wrapper.eq("name", datasetNodeDTO.getName());
        }
        if (StringUtils.isNotEmpty(datasetNodeDTO.getId())) {
            wrapper.ne("id", datasetNodeDTO.getId());
        }
        if (ObjectUtils.isNotEmpty(datasetNodeDTO.getLevel())) {
            wrapper.eq("level", datasetNodeDTO.getLevel());
        }
        if (ObjectUtils.isNotEmpty(datasetNodeDTO.getNodeType())) {
            wrapper.eq("node_type", datasetNodeDTO.getNodeType());
        }
        List<CoreDatasetGroup> list = coreDatasetGroupMapper.selectList(wrapper);
        if (list.size() > 0) {
            throw new RuntimeException("I18N_DATASET_TABLE_EXIST");
        }
    }
}
