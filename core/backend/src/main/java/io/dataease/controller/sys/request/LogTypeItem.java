package io.dataease.controller.sys.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LogTypeItem implements Serializable {

    private List<String> parentIds;

    private String typeValue;
}
