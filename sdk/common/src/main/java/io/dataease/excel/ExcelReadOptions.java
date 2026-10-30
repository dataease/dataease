package io.dataease.excel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
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
     * 目标 Sheet 编号，null 表示取第一个 Sheet；sheetName 优先于 sheetNo。
     */
    private Integer sheetNo;

    /**
     * 目标 Sheet 名称，优先于 sheetNo；找不到目标 Sheet 时抛出异常。
     */
    private String sheetName;

    /**
     * 按列索引指定的日期格式，由适配层内部转换器消费。
     */
    private Map<Integer, String> columnDateFormats = new HashMap<>();

    /**
     * 是否由适配层关闭输入流，默认 true（与 Fesod 默认一致）。
     * 设置为 false 时由调用方负责关闭输入流。
     */
    private Boolean autoCloseStream;

    /**
     * 仅读取指定原始列索引（0 基）；null 或空表示读取全部列。
     * 索引对应原始 Sheet 列号，适配层负责维持原始列映射。
     */
    private List<Integer> includeColumnIndexes;
}
