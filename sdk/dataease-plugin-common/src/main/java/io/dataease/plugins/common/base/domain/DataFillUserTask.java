package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class DataFillUserTask implements Serializable {
    private String id;

    private Long taskId;

    private String formId;

    private String valueId;

    private Long user;

    private Date startTime;

    private Date endTime;

    private Date finishTime;

    private static final long serialVersionUID = 1L;
}