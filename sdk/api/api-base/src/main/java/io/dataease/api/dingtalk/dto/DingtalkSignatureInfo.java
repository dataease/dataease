package io.dataease.api.dingtalk.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DingtalkSignatureInfo implements Serializable {

    private String corpId;

    private String agentId;

    private String timeStamp;

    private String nonceStr;

    private String signature;

    private Integer type = 0;

    private List<String> jsApiList = List.of("chooseChat");


}
