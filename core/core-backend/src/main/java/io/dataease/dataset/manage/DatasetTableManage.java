package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
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
public class DatasetTableManage {
    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;

    public void save(CoreDatasetTable coreDatasetTable) {
        if (StringUtils.isEmpty(coreDatasetTable.getId())) {
            coreDatasetTable.setId(UUID.randomUUID().toString());
            coreDatasetTableMapper.insert(coreDatasetTable);
        } else {
            coreDatasetTableMapper.updateById(coreDatasetTable);
        }
        // todo save field
    }

    public List<CoreDatasetTable> selectByDatasetGroupId(String datasetGroupId) {
        QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        return coreDatasetTableMapper.selectList(wrapper);
    }

    public CoreDatasetTable selectById(String datasetGroupId) {
        return coreDatasetTableMapper.selectById(datasetGroupId);
    }

    public void deleteByDatasetGroupUpdate(String datasetGroupId, List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_group_id", datasetGroupId);
            wrapper.notIn("id", ids);
            coreDatasetTableMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupDelete(String datasetGroupId) {
        coreDatasetTableMapper.deleteById(datasetGroupId);
    }
}
