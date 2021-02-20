package io.dataease.controller.request.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditPassWordRequest {
    private String password;
    private String newpassword;
    private String id;
}
