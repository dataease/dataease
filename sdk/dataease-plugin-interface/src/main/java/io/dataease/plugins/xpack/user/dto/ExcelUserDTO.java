package io.dataease.plugins.xpack.user.dto;

import java.io.Serializable;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ExcelUserDTO implements Serializable{

    @ExcelProperty(index = 0, value = "ID（必填，文本）")
    private String username;

    @ExcelProperty(index = 1, value = "姓名（必填，文本）")
    private String nickName;

    @ExcelProperty(index = 2, value = "性别（非必填，男或女）")
    private String gender;

    @ExcelProperty(index = 3, value = "邮箱（必填，文本）")
    private String email;

    @ExcelProperty(index = 4, value = "手机号（非必填，文本）")
    private String phone;

    @ExcelProperty(index = 5, value = "组织名称（非必填，文本）")
    private String deptName;

    @ExcelProperty(index = 6, value = "角色名称（非必填，文本）")
    private String roleNames;

    @ExcelProperty(index = 7, value = "用户状态（必填，是否启用）")
    private String enable;


    
}
