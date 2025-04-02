package io.dataease.api.permissions.user.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserGridRequest extends KeywordRequest implements Serializable {
    private List<Boolean> statusList;

    private List<Integer> originList;

    private List<Long> roleIdList;

    private Boolean timeDesc;
}
