package io.dataease.plugins.xpack.loginlimit.dto.response;

import lombok.Data;

@Data
public class LoginLimitInfo {

    private Integer limitTimes;

    private Integer relieveTimes;

    private String open;

    private String scanCreateUser;

    private String multiLogin;

    private String lockedEmail = "false";

    private String openModifyPwd;

    private String pwdCycle = "1";
}
