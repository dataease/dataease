package io.dataease.dataset.server;

import io.dataease.api.dataset.DataAssistantApi;
import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import io.dataease.api.dataset.vo.DataSQLBotDatasetVO;
import io.dataease.dataset.manage.DatasetSQLBotManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sqlbot")
public class DatasetSQLBotServer implements DataAssistantApi {

    @Resource
    private DatasetSQLBotManage datasetSQLBotManage;
    @Override
    public List<DataSQLBotAssistantVO> getDatasourceList(Long dsId, Long tableId) {
        return datasetSQLBotManage.getDatasourceList(dsId, tableId);
    }

    @Override
    public List<DataSQLBotDatasetVO> getDatasetList(String dvInfo) {
        return datasetSQLBotManage.getDatasetList(dvInfo);
    }
}
