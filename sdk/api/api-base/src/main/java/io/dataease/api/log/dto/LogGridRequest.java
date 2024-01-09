package io.dataease.api.log.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogGridRequest extends KeywordRequest implements Serializable {

    private String op;

    private Long uid;

    private Long oid;

    private List<Long> time;
}
