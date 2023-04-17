package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
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
public class DatasetTableManage {
    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;

    public void save(CoreDatasetTable coreDatasetTable) {
        if (ObjectUtils.isEmpty(coreDatasetTable.getId())) {
            coreDatasetTable.setId(IDUtils.snowID());
            coreDatasetTableMapper.insert(coreDatasetTable);
        } else {
            coreDatasetTableMapper.updateById(coreDatasetTable);
        }
    }

    public void save(DatasetTableDTO currentDs) {
        CoreDatasetTable coreDatasetTable = coreDatasetTableMapper.selectById(currentDs.getId());
        CoreDatasetTable record = new CoreDatasetTable();
        BeanUtils.copyBean(record, currentDs);
        if (ObjectUtils.isEmpty(coreDatasetTable)) {
            coreDatasetTableMapper.insert(record);
        } else {
            coreDatasetTableMapper.updateById(record);
        }
    }

    public List<CoreDatasetTable> selectByDatasetGroupId(Long datasetGroupId) {
        QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        return coreDatasetTableMapper.selectList(wrapper);
    }

    public CoreDatasetTable selectById(Long id) {
        return coreDatasetTableMapper.selectById(id);
    }

    public void deleteByDatasetGroupUpdate(Long datasetGroupId, List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_group_id", datasetGroupId);
            wrapper.notIn("id", ids);
            coreDatasetTableMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupDelete(Long datasetGroupId) {
        QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        coreDatasetTableMapper.delete(wrapper);
    }
}
