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
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void save(DatasetTableDTO currentDs) {
        checkNameLength(currentDs.getName());
        checkNameLength(currentDs.getTableName());
        CoreDatasetTable coreDatasetTable = coreDatasetTableRepository.findById(currentDs.getId()).orElse(null);
        if (coreDatasetTable == null) {
            CoreDatasetTable record = new CoreDatasetTable();
            BeanUtils.copyBean(record, currentDs);
            coreDatasetTableRepository.saveAndFlush(record);
            return;
        }
        // 已存在时显式完整替换可修改字段，依赖事务脏检查落库，避免非空合并切面把 null 当作未提供。
        coreDatasetTable.setName(currentDs.getName());
        coreDatasetTable.setTableName(currentDs.getTableName());
        coreDatasetTable.setDatasourceId(currentDs.getDatasourceId());
        coreDatasetTable.setDatasetGroupId(currentDs.getDatasetGroupId());
        coreDatasetTable.setType(currentDs.getType());
        coreDatasetTable.setInfo(currentDs.getInfo());
        coreDatasetTable.setSqlVariableDetails(currentDs.getSqlVariableDetails());
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
