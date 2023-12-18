package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LarkGroupResult implements Serializable {

    private boolean valid;

    private List<LarkGroupItem> groupList;
}
