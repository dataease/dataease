package io.dataease.mobile.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DirRequest implements Serializable {

    private String pid;

    private String name;

}
