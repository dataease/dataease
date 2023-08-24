package io.dataease.plugins.xpack.dingtalk.dto.entity;

import io.dataease.plugins.xpack.wecom.dto.entity.BaseResult;
import lombok.Data;

@Data
public class UserIdResult extends BaseResult {

    private UserIdEntity result;

    private String request_id;
}
