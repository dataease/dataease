package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataFillTaskWithBLOBs extends DataFillTask implements Serializable {
    private String reciUsers;

    private String roleList;

    private String orgList;

    private static final long serialVersionUID = 1L;
}