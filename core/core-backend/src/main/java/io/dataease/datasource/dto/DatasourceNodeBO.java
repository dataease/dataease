package io.dataease.datasource.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.ds.vo.TaskDTO;
import io.dataease.model.TreeBaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasourceNodeBO implements TreeBaseModel {

    @Serial
    private static final long serialVersionUID = 728340676442387790L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;

    private String type;
    private String typeAlias;

    private String catalog;

    private String catalogDesc;

    private Boolean leaf;
    private Integer weight = 3;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long pid;
    private Integer extraFlag;

    private String configuration;

    private String apiConfigurationStr;
    private String status;

    private TaskDTO syncSetting;

    private Integer editType;
    private String  nodeType;
}
