package io.dataease.api.lark.vo;

import io.dataease.api.lark.dto.LarkGroupItem;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class LarkGroupVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 39710350567348130L;

    private boolean valid;
    private List<LarkGroupItem> groupList;
}
