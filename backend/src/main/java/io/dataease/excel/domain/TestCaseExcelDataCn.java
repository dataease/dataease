package io.dataease.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ColumnWidth(15)
public class TestCaseExcelDataCn extends TestCaseExcelData {

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("用例名称")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("所属模块")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("用例类型")
    @Pattern(regexp = "(^functional$)|(^performance$)|(^api$)", message = "{test_case_type_validate}")
    private String type;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("维护人")
    private String maintainer;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("用例等级")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("测试方式")
    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
    private String method;

    @ColumnWidth(50)
    @ExcelProperty("前置条件")
    @Length(min = 0, max = 1000)
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("备注")
    @Length(max = 1000)
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("步骤描述")
    @Length(max = 1000)
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("预期结果")
    @Length(max = 1000)
    private String stepResult;
}
