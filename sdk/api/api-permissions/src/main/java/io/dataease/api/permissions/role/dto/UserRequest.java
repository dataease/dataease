package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserRequest extends KeywordRequest  {

    @Serial
    private static final long serialVersionUID = -2740015284392981297L;
    private Long rid;

    private String order;

}
