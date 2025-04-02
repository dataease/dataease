package io.dataease.extensions.view.dto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChartSeniorFunctionCfgDTO {
    private String emptyDataStrategy;
    private String emptyDataCustomValue;
    private List<String> emptyDataFieldCtrl = new ArrayList<>();
}
