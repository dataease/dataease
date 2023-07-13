package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetDataApi;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("datasetData")
public class DatasetDataServer implements DatasetDataApi {
    @Resource
    private DatasetDataManage datasetDataManage;

    @Override
    public Map<String, Object> previewData(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        return datasetDataManage.previewDataWithLimit(datasetGroupInfoDTO, 0, 100);
    }

    @Override
    public List<DatasetTableFieldDTO> tableField(DatasetTableDTO datasetTableDTO) throws Exception {
        return datasetDataManage.getTableFields(datasetTableDTO);
    }

    @Override
    public Map<String, Object> previewSql(PreviewSqlDTO dto) throws Exception {
        return datasetDataManage.previewSql(dto);
    }

    @Override
    public Map<String, Object> getFieldEnum(Long id) throws Exception {
        return datasetDataManage.getFieldEnum(id);
    }
}
