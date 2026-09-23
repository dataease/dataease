package io.dataease.api.spreadsheet.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class DetailTableQueryDataResponse extends PluginQueryDataResponse {
    private Map<String, BigDecimal> customTotalResult;
}
