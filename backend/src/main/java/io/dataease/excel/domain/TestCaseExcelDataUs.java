package io.dataease.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


@Data
@ColumnWidth(15)
public class TestCaseExcelDataUs extends TestCaseExcelData {

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Name")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("Module")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("Type")
    @Pattern(regexp = "(^functional$)|(^performance$)|(^api$)", message = "{test_case_type_validate}")
    private String type;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("Maintainer")
    private String maintainer;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("Priority")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("Method")
    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
    private String method;

    @ColumnWidth(50)
    @ExcelProperty("Prerequisite")
    @Length(min = 0, max = 1000)
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("Remark")
    @Length(max = 1000)
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("Step description")
    @Length(max = 1000)
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("Step result")
    @Length(max = 1000)
    private String stepResult;
}
