package io.dataease.extensions.view.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author Junjun
 */
@Data
@Accessors(chain = true)
public class FormatterCfgDTO {
    private String type = "auto"; // auto,value,percent
    private String unitLanguage = "ch";
    private Integer unit = 1; // 换算单位
    private String suffix = ""; // 单位后缀
    private Integer decimalCount = 0; // 小数位数
    private Boolean thousandSeparator = false; // 千分符
}
