package io.dataease.dataset.server;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.dataset.DatasetTableApi;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.api.dataset.dto.MultFieldValuesRequest;
import io.dataease.api.dataset.engine.SQLFunctionDTO;
import io.dataease.api.dataset.engine.SQLFunctionsEnum;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("datasetField")
public class DatasetFieldServer implements DatasetTableApi {
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

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
    public List<Object> multFieldValuesForPermissions(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        List<Object> results = new ArrayList<>();
        for (Long fieldId : multFieldValuesRequest.getFieldIds()) {
            List<Object> fieldValues = datasetTableFieldManage.fieldValues(fieldId, multFieldValuesRequest.getUserId(), false, true);
            if (CollectionUtil.isNotEmpty(fieldValues)) {
                results.addAll(fieldValues);
            }

        }
        ArrayList<Object> list = results.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(t -> {
                                    if (ObjectUtils.isEmpty(t))
                                        return "";
                                    return t.toString();
                                }))),
                        ArrayList::new));
        return list;
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
}
