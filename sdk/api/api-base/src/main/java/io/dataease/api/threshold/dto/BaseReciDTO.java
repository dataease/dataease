package io.dataease.api.threshold.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BaseReciDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1996467050556455121L;

    private List<Integer> reciFlagList;

    private List<Long> uidList;

    private List<Long> ridList;

    private List<String> emailList;

    private List<String> larkGroupList;

    private List<String> webhookList;
}
