package io.dataease.dto.log;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class LogExcel {

    @ExcelProperty(value = {"optype"}, index = 0)
    private String optype;

    @ExcelProperty(value = {"detail"}, index = 1)
    private String detail;

    @ExcelProperty(value = {"user"}, index = 2)
    private String user;

    @ExcelProperty(value = {"time"}, index = 3)
    private String time;
}
