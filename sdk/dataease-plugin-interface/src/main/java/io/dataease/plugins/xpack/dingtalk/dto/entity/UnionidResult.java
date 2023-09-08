package io.dataease.plugins.xpack.dingtalk.dto.entity;

import io.dataease.plugins.xpack.wecom.dto.entity.BaseResult;
import lombok.Data;

@Data
public class UnionidResult extends BaseResult {

    private UnionidEntity user_info;
}
