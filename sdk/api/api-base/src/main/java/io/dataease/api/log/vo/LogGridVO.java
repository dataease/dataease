package io.dataease.api.log.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LogGridVO implements Serializable {

    private String opText;

    private String opDetail;

    private String name;

    private String ip;

    private Long time;

    private boolean success;

    private String msg;
}
