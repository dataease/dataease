package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetDataApi;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
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
    public List<String> getFieldEnum(List<Long> ids) {
        try{
           return datasetDataManage.getFieldEnum(ids);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.error(e);
            return null;
        }

    }

    @Override
    public Long getDatasetCount(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        return datasetDataManage.getDatasetTotal(datasetGroupInfoDTO.getId());
    }
}
