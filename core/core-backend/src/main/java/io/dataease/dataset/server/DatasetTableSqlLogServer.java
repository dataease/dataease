package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetTableSqlLogApi;
import io.dataease.api.dataset.dto.SqlLogDTO;
import io.dataease.dataset.manage.DatasetTableSqlLogManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("datasetTableSqlLog")
public class DatasetTableSqlLogServer implements DatasetTableSqlLogApi {
    @Resource
    private DatasetTableSqlLogManage datasetTableSqlLogManage;

    @Override
    public void save(SqlLogDTO sqlLogDTO) throws Exception {
        datasetTableSqlLogManage.save(sqlLogDTO);
    }

    @Override
    public List<SqlLogDTO> listByTableId(SqlLogDTO sqlLogDTO) throws Exception {
        return datasetTableSqlLogManage.listByTableId(sqlLogDTO);
    }

    @Override
    public void deleteByTableId(String id) throws Exception {
        datasetTableSqlLogManage.deleteByTableId(id);
    }
}
