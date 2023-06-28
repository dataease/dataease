package io.dataease.api.chart.dto;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class FormatterCfgDTO {
    private String type = "auto"; // auto,value,percent
    private Integer unit = 1; // 换算单位
    private String suffix = ""; // 单位后缀
    private Integer decimalCount = 2; // 小数位数
    private Boolean thousandSeparator = true; // 千分符
}
