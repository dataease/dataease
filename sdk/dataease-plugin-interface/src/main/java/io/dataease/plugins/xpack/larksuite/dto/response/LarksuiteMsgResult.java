package io.dataease.plugins.xpack.larksuite.dto.response;

import io.dataease.plugins.xpack.lark.dto.entity.LarkBaseResult;
import io.dataease.plugins.xpack.larksuite.dto.entity.MsgData;
import lombok.Data;

import java.io.Serializable;

@Data
public class LarksuiteMsgResult extends LarkBaseResult implements Serializable {

    private MsgData data;
}
