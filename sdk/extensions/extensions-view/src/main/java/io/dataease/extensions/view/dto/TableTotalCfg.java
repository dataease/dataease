package io.dataease.extensions.view.dto;

import lombok.Data;

@Data
public class TableTotalCfg {
    private boolean showGrandTotals;
    private boolean showSubTotals;
    private TableCalcTotal calcTotals;
    private TableCalcTotal calcSubTotals;
}
