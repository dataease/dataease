package io.dataease.datasource.dao.ext.po;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DsItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 370886385725258461L;

    private Long id;

    private Long pid;
}
