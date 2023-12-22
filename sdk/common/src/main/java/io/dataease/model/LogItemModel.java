package io.dataease.model;

import io.dataease.constant.LogST;
import lombok.Data;

import java.io.Serializable;

@Data
public class LogItemModel implements Serializable {

    private Long id;

    private String name;

    private LogST st;
}
