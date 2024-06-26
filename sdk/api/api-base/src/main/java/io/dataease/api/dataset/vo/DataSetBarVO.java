package io.dataease.api.dataset.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DataSetBarVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7791029875759340927L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    private String nodeType;

    private String createBy;

    private Long createTime;

    private String creator;

    private Long lastUpdateTime;

    private String updateBy;

    private String updater;

    private List<DatasourceDTO> datasourceDTOList;
}
