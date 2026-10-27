package io.dataease.excel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ExcelWriteOptions {

    private String sheetName;

    private List<List<String>> head = new ArrayList<>();

    /**
     * 表头批注，由适配层内部使用 POI 绘制。
     */
    private List<ExcelHeaderComment> headerComments = new ArrayList<>();

    /**
     * 错误批注：每个数据行对应一个 列索引 -> 错误信息 的 Map。
     */
    private List<Map<Integer, String>> errMsgList = new ArrayList<>();

    private Boolean inMemory;

    private Boolean autoCloseStream;

    private Boolean automaticMergeHead;
}
