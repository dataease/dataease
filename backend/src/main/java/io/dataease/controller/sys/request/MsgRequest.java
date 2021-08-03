package io.dataease.controller.sys.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MsgRequest implements Serializable {

    private static final long serialVersionUID = 1920091635946508658L;

    private Long type;

    private Boolean status;

    private List<String> orders;
}
