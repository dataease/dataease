package io.dataease.plugins.xpack.larksuite.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserData implements Serializable {

    private String name;
    private String en_name;
    private String open_id;
    private String union_id;
    private String email;
    private String enterprise_email;
    private String user_id;
    private String mobile;
    private String tenant_key;
}
