package io.dataease.plugins.xpack.dingtalk.dto.entity;

import io.dataease.plugins.xpack.wecom.dto.entity.BaseResult;
import lombok.Data;

import java.io.Serializable;

@Data
public class DingtalkMsgResult extends BaseResult implements Serializable {

    private String task_id;

    private String request_id;
}
