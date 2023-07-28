package io.dataease.xpack.permissions.user.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExcelUserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8615861950756447715L;

    @ExcelProperty(index = 0, value = {"ID（必填，文本）"})
    private String account;

    @ExcelProperty(index = 1,value = {"姓名（必填，文本）"})
    private String name;

    @ExcelProperty(index = 3,value = {"邮箱（必填，文本）"})
    private String email;

    @ExcelProperty(index = 4,value = {"手机号（非必填，文本）"})
    private String phone;

    @ExcelProperty(index = 7,value = {"用户状态（必填，是否启用）"})
    private String enable;
}
