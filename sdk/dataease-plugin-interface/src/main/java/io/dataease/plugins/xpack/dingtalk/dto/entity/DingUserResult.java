package io.dataease.plugins.xpack.dingtalk.dto.entity;

import io.dataease.plugins.xpack.dingtalk.dto.response.DingUserEntity;
import io.dataease.plugins.xpack.wecom.dto.entity.BaseResult;
import lombok.Data;

@Data
public class DingUserResult extends BaseResult {

    private DingUserEntity result;

    private String request_id;
}
