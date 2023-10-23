package io.dataease.api.permissions.org.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;


@EqualsAndHashCode(callSuper = true)
@Data
public class OrgRequest extends KeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1697526057837588192L;

    private Boolean desc = true;
}
