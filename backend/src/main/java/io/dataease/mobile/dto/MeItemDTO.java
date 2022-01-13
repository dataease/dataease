package io.dataease.mobile.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class MeItemDTO implements Serializable {
    private String deptName;
    private Long createTime;
}
