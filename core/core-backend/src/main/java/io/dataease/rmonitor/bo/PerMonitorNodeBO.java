package io.dataease.rmonitor.bo;

import io.dataease.model.TreeBaseModel;
import io.dataease.model.TreeResultModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PerMonitorNodeBO implements TreeBaseModel<PerMonitorNodeBO>, TreeResultModel<PerMonitorNodeBO>, Serializable {

    private Long id;

    private String name;

    private Long pid;

    private boolean leaf;

    private int extraFlag;

    private List<PerMonitorNodeBO> children;
}
