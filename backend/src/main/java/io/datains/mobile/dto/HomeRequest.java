package io.datains.mobile.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HomeRequest implements Serializable {

    private Integer type;

    private Long lastTime;
}
