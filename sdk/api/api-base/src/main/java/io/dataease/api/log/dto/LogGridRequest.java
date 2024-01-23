package io.dataease.api.log.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogGridRequest extends KeywordRequest implements Serializable {

    private List<String> op;

    private List<Long> uid;

    private List<Long> oid;

    private List<Long> time;

    private Boolean timeDesc = true;
}
