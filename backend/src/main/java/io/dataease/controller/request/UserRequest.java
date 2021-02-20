package io.dataease.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String id;
    private String name;
    private String email;
}
