package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetTableApi;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("datasetTable")
public class DatasetFieldService implements DatasetTableApi {
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

    @Override
    public DatasetTableFieldDTO save(DatasetTableFieldDTO datasetTableFieldDTO) throws Exception {
        return datasetTableFieldManage.save(datasetTableFieldDTO);
    }

    @Override
    public DatasetTableFieldDTO get(Long id) {
        return datasetTableFieldManage.selectById(id);
    }

    @Override
    public List<DatasetTableFieldDTO> listByDatasetGroup(Long id) {
        return datasetTableFieldManage.selectByDatasetGroupId(id);
    }

    @Override
    public void delete(Long id) {
        datasetTableFieldManage.deleteById(id);
    }

}
