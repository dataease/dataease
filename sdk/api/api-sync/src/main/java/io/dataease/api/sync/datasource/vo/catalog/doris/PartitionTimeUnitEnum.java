package io.dataease.api.sync.datasource.vo.catalog.doris;

import lombok.Getter;

/**
 * 分区时间间隔单位枚举
 * @author fit2cloud
 **/
@Getter
public enum PartitionTimeUnitEnum {
    HOUR("HOUR","yyyyMMddHH","yyyyMMddHH"),
    DAY("DAY","yyyyMMdd","yyyyMMdd"),
    WEEK("WEEK","yyyy_ww","yyyy_ww"),
    MONTH("MONTH","yyyyMM","yyyyMM"),
    YEAR("YEAR","yyyyMM","yyyyMM");
   private final String unit;
   private final String nameFormatter;
   private final String valueFormatter;
    PartitionTimeUnitEnum(String unit, String nameFormatter, String valueFormatter) {
        this.unit = unit;
       this.nameFormatter = nameFormatter;
       this.valueFormatter = valueFormatter;
    }
}
