package io.dataease.dataset.manage;

import io.dataease.api.dataset.dto.SqlLogDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableSqlLog;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableSqlLogRepository;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DatasetTableSqlLogManage {
    @Resource
    private CoreDatasetTableSqlLogRepository coreDatasetTableSqlLogRepository;

    public void save(SqlLogDTO dto) {
        if (dto == null) {
            return;
        }
        CoreDatasetTableSqlLog coreDatasetTableSqlLog = new CoreDatasetTableSqlLog();
        BeanUtils.copyBean(coreDatasetTableSqlLog, dto);
        if (ObjectUtils.isEmpty(coreDatasetTableSqlLog.getId())) {
            coreDatasetTableSqlLog.setId(UUID.randomUUID().toString());
            coreDatasetTableSqlLogRepository.saveAndFlush(coreDatasetTableSqlLog);
        } else {
            coreDatasetTableSqlLogRepository.saveAndFlush(coreDatasetTableSqlLog);
        }
    }

    public List<SqlLogDTO> listByTableId(SqlLogDTO dto) {
        if (dto == null || ObjectUtils.isEmpty(dto.getTableId())) {
            return null;
        }
        List<CoreDatasetTableSqlLog> coreDatasetTableSqlLogs = coreDatasetTableSqlLogRepository.findByTableId(dto.getTableId());
        return coreDatasetTableSqlLogs.stream().map(ele -> {
            SqlLogDTO s = new SqlLogDTO();
            BeanUtils.copyBean(s, ele);
            return s;
        }).collect(Collectors.toList());
    }

    public void deleteByTableId(String id) {
        coreDatasetTableSqlLogRepository.deleteByTableId(id);
    }
}
