package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
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

    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        checkName(datasetGroupInfoDTO);
        // todo node_type=dataset需要创建dataset_table和field
        if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), "dataset")) {
            // todo coding...
        }
        // save dataset group
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        if (StringUtils.isEmpty(datasetGroupInfoDTO.getId())) {
            datasetGroupInfoDTO.setId(UUID.randomUUID().toString());
            datasetGroupInfoDTO.setCreateBy("admin");// todo username
            datasetGroupInfoDTO.setCreateTime(System.currentTimeMillis());
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.insert(coreDatasetGroup);
        } else {
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.updateById(coreDatasetGroup);
        }
        return datasetGroupInfoDTO;
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

    private void checkName(DatasetGroupInfoDTO dto) {
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(dto.getPid())) {
            wrapper.eq("pid", dto.getPid());
        }
        if (StringUtils.isNotEmpty(dto.getName())) {
            wrapper.eq("name", dto.getName());
        }
        if (StringUtils.isNotEmpty(dto.getId())) {
            wrapper.ne("id", dto.getId());
        }
        if (ObjectUtils.isNotEmpty(dto.getLevel())) {
            wrapper.eq("level", dto.getLevel());
        }
        if (ObjectUtils.isNotEmpty(dto.getNodeType())) {
            wrapper.eq("node_type", dto.getNodeType());
        }
        List<CoreDatasetGroup> list = coreDatasetGroupMapper.selectList(wrapper);
        if (list.size() > 0) {
            throw new RuntimeException("I18N_DATASET_TABLE_EXIST");
        }
    }
}
