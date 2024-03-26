package io.dataease.controller.request.datafill;

import io.dataease.plugins.common.base.domain.DataFillForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;


@Data
@Accessors(chain=true)
public class DataFillFormRequest extends DataFillForm {

    private static final long serialVersionUID = -6752223673862214909L;

    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("ID集合")
    private Set<String> ids;
    @ApiModelProperty("排除的ID")
    private String excludedId;
}
