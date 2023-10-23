package io.dataease.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3038086304525253475L;
    private String keyword;
}
