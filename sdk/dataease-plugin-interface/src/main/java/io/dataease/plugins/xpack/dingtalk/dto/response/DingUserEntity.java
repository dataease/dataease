package io.dataease.plugins.xpack.dingtalk.dto.response;

import lombok.Data;

@Data
public class DingUserEntity {

    private String userid;
    private String unionid;
    private String name;
    private String mobile;
    private String email;
    private String org_email;

}
