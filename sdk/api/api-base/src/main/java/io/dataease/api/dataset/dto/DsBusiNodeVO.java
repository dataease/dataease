package io.dataease.api.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.ds.vo.TaskDTO;
import io.dataease.model.BusiNodeVO;
import io.dataease.model.TreeResultModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "业务资源结点")
@Data
public class DsBusiNodeVO extends BusiNodeVO {

    @Serial
    private static final long serialVersionUID = 728340676442387790L;

    private String type;
    private String typeAlias;

    private String catalog;

    private String catalogDesc;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long pid;

    private String configuration;

    private String apiConfigurationStr;
    private String status;

    private TaskDTO syncSetting;

    private Integer editType;
    private String  nodeType;

}
