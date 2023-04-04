package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

/**
 * @Author Junjun
 */
@Component
public class DatasetTableFieldManage {
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;

    public void save(CoreDatasetTableField coreDatasetTableField) {
        if (StringUtils.isEmpty(coreDatasetTableField.getId())) {
            coreDatasetTableField.setId(UUID.randomUUID().toString());
            coreDatasetTableFieldMapper.insert(coreDatasetTableField);
        } else {
            coreDatasetTableFieldMapper.updateById(coreDatasetTableField);
        }
    }

    public void deleteByDatasetTableUpdate(String datasetTableId, List<String> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_table_id", datasetTableId);
            wrapper.notIn("id", fieldIds);
            coreDatasetTableFieldMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupUpdate(String datasetGroupId, List<String> datasetTableIds) {
        if (!CollectionUtils.isEmpty(datasetTableIds)) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_group_id", datasetGroupId);
            wrapper.notIn("dataset_table_id", datasetTableIds);
            coreDatasetTableFieldMapper.delete(wrapper);
        }
    }

    public List<CoreDatasetTableField> selectByDatasetTableId(String id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_table_id", id);
        return coreDatasetTableFieldMapper.selectList(wrapper);
    }

    public List<CoreDatasetTableField> selectByDatasetGroupId(String id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", id);
        return coreDatasetTableFieldMapper.selectList(wrapper);
    }

    public List<CoreDatasetTableField> selectByFieldIds(List<String> ids) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        return coreDatasetTableFieldMapper.selectList(wrapper);
    }

    public CoreDatasetTableField selectById(String id) {
        return coreDatasetTableFieldMapper.selectById(id);
    }
}
