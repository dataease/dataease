package io.dataease.dataset.manage;

import io.dataease.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableRepository;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.i18n.Translator;
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
    private CoreDatasetTableRepository coreDatasetTableRepository;

    public void save(CoreDatasetTable coreDatasetTable) {
        checkNameLength(coreDatasetTable.getName());
        checkNameLength(coreDatasetTable.getTableName());
        if (ObjectUtils.isEmpty(coreDatasetTable.getId())) {
            coreDatasetTable.setId(IDUtils.snowID());
            coreDatasetTableRepository.saveAndFlush(coreDatasetTable);
        } else {
            coreDatasetTableRepository.saveAndFlush(coreDatasetTable);
        }
    }

    public void save(DatasetTableDTO currentDs) {
        checkNameLength(currentDs.getName());
        checkNameLength(currentDs.getTableName());
        CoreDatasetTable coreDatasetTable = coreDatasetTableRepository.findById(currentDs.getId()).orElse(null);
        CoreDatasetTable record = new CoreDatasetTable();
        BeanUtils.copyBean(record, currentDs);
        if (ObjectUtils.isEmpty(coreDatasetTable)) {
            coreDatasetTableRepository.saveAndFlush(record);
        } else {
            coreDatasetTableRepository.saveAndFlush(record);
        }
    }

    public List<CoreDatasetTable> selectByDatasetGroupId(Long datasetGroupId) {
        return coreDatasetTableRepository.findByDatasetGroupId(datasetGroupId);

    }

    public CoreDatasetTable selectById(Long id) {
        return coreDatasetTableRepository.findById(id).orElse(null);
    }

    public void deleteByDatasetGroupUpdate(Long datasetGroupId, List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            coreDatasetTableRepository.deleteByDatasetGroupIdAndNotInIds(datasetGroupId, ids);
        }
    }

    public void deleteByDatasetGroupDelete(Long datasetGroupId) {
        coreDatasetTableRepository.deleteByDatasetGroupId(datasetGroupId);
    }

    private void checkNameLength(String name) {
        if (name != null && name.length() > 100) {
            DEException.throwException(Translator.get("i18n_name_limit_100"));
        }
    }
}
