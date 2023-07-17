package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetTableApi;
import io.dataease.api.dataset.dto.MultFieldValuesRequest;
import io.dataease.api.dataset.engine.SQLFunctionDTO;
import io.dataease.api.dataset.engine.SQLFunctionsEnum;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("datasetField")
public class DatasetFieldServer implements DatasetTableApi {
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private DatasetDataManage datasetDataManage;

    @Override
    public DatasetTableFieldDTO save(DatasetTableFieldDTO datasetTableFieldDTO) throws Exception {
        return datasetTableFieldManage.chartFieldSave(datasetTableFieldDTO);
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

    @Override
    public Map<String, List<DatasetTableFieldDTO>> listByDQ(Long id) {
        return datasetTableFieldManage.listByDQ(id);
    }

    @Override
    public List<String> multFieldValuesForPermissions(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        return datasetDataManage.getFieldEnum(multFieldValuesRequest.getFieldIds());
    }

    @Override
    public List<SQLFunctionDTO> getFunction() {
        SQLFunctionsEnum[] values = SQLFunctionsEnum.values();
        return Arrays.stream(values).map(ele -> {
            SQLFunctionDTO dto = new SQLFunctionDTO();
            dto.setName(ele.getName());
            dto.setFunc(ele.getFunc());
            dto.setType(ele.getType());
            dto.setDesc(ele.getDesc());
            dto.setCustom(ele.isCustom());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteByChartId(Long id) {
        datasetTableFieldManage.deleteByChartId(id);
    }
}
