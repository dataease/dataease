package io.dataease.api.permissions.org.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrgLazyRequest extends KeywordRequest implements Serializable {

    private Long pid;
}
