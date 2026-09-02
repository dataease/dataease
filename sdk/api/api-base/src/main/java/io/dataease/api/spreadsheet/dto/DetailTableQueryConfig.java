package io.dataease.api.spreadsheet.dto;

import io.dataease.extensions.view.dto.TableCalcTotalCfg;
import lombok.Data;

import java.util.List;

@Data
public class DetailTableQueryConfig {
    private List<TableCalcTotalCfg> totalFields;
}
