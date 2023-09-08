package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkUserInfo implements Serializable {

    private String sub;
    private String name;
    private String open_id;
    private String union_id;
    private String en_name;
    private String email;
    private String user_id;
    private String mobile;
}
