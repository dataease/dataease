package io.dataease.extensions.view.dto;

import lombok.Data;

import java.util.List;

@Data
public class TableCalcTotal {
    private List<TableCalcTotalCfg> cfg;
}
