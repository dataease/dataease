package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetDataApi;
import io.dataease.api.dataset.dto.BaseTreeNodeDTO;
import io.dataease.api.dataset.dto.EnumValueRequest;
import io.dataease.api.dataset.dto.MultFieldValuesRequest;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.utils.LogUtil;
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
        return datasetDataManage.previewDataWithLimit(datasetGroupInfoDTO, 0, 100, false);
    }

    @Override
    public List<DatasetTableFieldDTO> tableField(DatasetTableDTO datasetTableDTO) throws Exception {
        return datasetDataManage.getTableFields(datasetTableDTO);
    }

    @Override
    public Map<String, Object> previewSql(PreviewSqlDTO dto) throws Exception {
        return datasetDataManage.previewSqlWithLog(dto);
    }

    @Override
    public Map<String, Object> previewSqlCheck(PreviewSqlDTO dto) throws Exception {
        return datasetDataManage.previewSql(dto);
    }

    @Override
    public List<String> getFieldEnum(MultFieldValuesRequest multFieldValuesRequest) {
        try {
            return datasetDataManage.getFieldEnum(multFieldValuesRequest);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getFieldEnumObj(EnumValueRequest request) throws Exception {
        try {
            return datasetDataManage.getFieldEnumObj(request);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e);
            return null;
        }
    }

    @Override
    public Long getDatasetCount(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        return datasetDataManage.getDatasetTotal(datasetGroupInfoDTO.getId());
    }

    @Override
    public Long getDatasetTotal(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        return datasetDataManage.getDatasetCountWithWhere(datasetGroupInfoDTO.getId());
    }

    @Override
    public List<BaseTreeNodeDTO> getFieldValueTree(MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        try {
            return datasetDataManage.getFieldValueTree(multFieldValuesRequest);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e);
            return null;
        }
    }
}
