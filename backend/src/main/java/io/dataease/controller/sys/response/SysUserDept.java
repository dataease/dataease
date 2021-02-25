package io.dataease.controller.sys.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserDept implements Serializable {

    private Long deptId;

    private Long pid;

    private String deptName;
}
