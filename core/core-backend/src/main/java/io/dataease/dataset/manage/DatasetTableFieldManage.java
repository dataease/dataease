package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author Junjun
 */
@Component
public class DatasetTableFieldManage {
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;

    public void save(CoreDatasetTableField coreDatasetTableField) {
        if (ObjectUtils.isEmpty(coreDatasetTableField.getId())) {
            coreDatasetTableField.setId(IDUtils.snowID());
            coreDatasetTableFieldMapper.insert(coreDatasetTableField);
        } else {
            coreDatasetTableFieldMapper.updateById(coreDatasetTableField);
        }
    }

    // 数据集保存时使用
    public DatasetTableFieldDTO save(DatasetTableFieldDTO datasetTableFieldDTO) {
//        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(datasetTableFieldDTO.getId());
//        CoreDatasetTableField record = new CoreDatasetTableField();
//        BeanUtils.copyBean(record, datasetTableFieldDTO);
//        if (ObjectUtils.isEmpty(coreDatasetTableField)) {
//            coreDatasetTableFieldMapper.insert(record);
//        } else {
//            coreDatasetTableFieldMapper.updateById(record);
//        }

        CoreDatasetTableField record = new CoreDatasetTableField();
        if (ObjectUtils.isEmpty(datasetTableFieldDTO.getId())) {
            datasetTableFieldDTO.setId(IDUtils.snowID());
            BeanUtils.copyBean(record, datasetTableFieldDTO);
            coreDatasetTableFieldMapper.insert(record);
        } else {
            BeanUtils.copyBean(record, datasetTableFieldDTO);
            coreDatasetTableFieldMapper.updateById(record);
        }
        return datasetTableFieldDTO;
    }

    public void deleteById(Long id) {
        coreDatasetTableFieldMapper.deleteById(id);
    }

    public void deleteByDatasetTableUpdate(Long datasetTableId, List<Long> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_table_id", datasetTableId);
            wrapper.notIn("id", fieldIds);
            coreDatasetTableFieldMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupUpdate(Long datasetGroupId, List<Long> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_group_id", datasetGroupId);
            wrapper.notIn("id", fieldIds);
            coreDatasetTableFieldMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupDelete(Long datasetGroupId) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        coreDatasetTableFieldMapper.delete(wrapper);
    }

    public List<CoreDatasetTableField> selectByDatasetTableId(Long id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_table_id", id);
        return coreDatasetTableFieldMapper.selectList(wrapper);
    }

    public List<CoreDatasetTableField> selectByDatasetGroupId(Long id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", id);
        return coreDatasetTableFieldMapper.selectList(wrapper);
    }

    public List<CoreDatasetTableField> selectByFieldIds(List<Long> ids) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        return coreDatasetTableFieldMapper.selectList(wrapper);
    }

    public CoreDatasetTableField selectById(Long id) {
        return coreDatasetTableFieldMapper.selectById(id);
    }
}
