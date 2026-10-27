package io.dataease.excel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ExcelReadOptions {

    /**
     * 表头行数，默认 1（与 Fesod 默认一致）。
     */
    private Integer headRowNumber = 1;

    /**
     * 目标 Sheet 编号，null 表示取第一个 Sheet。
     */
    private Integer sheetNo;

    /**
     * 目标 Sheet 名称，优先于 sheetNo。
     */
    private String sheetName;

    /**
     * 按列索引指定的日期格式，由适配层内部转换器消费。
     */
    private Map<Integer, String> columnDateFormats = new HashMap<>();
}
