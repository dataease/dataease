package io.dataease.plugins.xpack.lark.dto.response;

import io.dataease.plugins.xpack.lark.dto.entity.LarkAppUserEntity;
import io.dataease.plugins.xpack.lark.dto.entity.LarkBaseResult;
import lombok.Data;

import java.io.Serializable;

@Data
public class LarkAppUserResult extends LarkBaseResult implements Serializable {

    private LarkAppUserEntity data;
}
