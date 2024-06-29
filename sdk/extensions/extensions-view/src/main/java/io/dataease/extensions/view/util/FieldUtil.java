package io.dataease.extensions.view.util;

import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewFieldBaseDTO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class FieldUtil {
    public static List<DatasetTableFieldDTO> transFields(List<? extends ChartViewFieldBaseDTO> list) {
        return list.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            BeanUtils.copyProperties(ele, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}
