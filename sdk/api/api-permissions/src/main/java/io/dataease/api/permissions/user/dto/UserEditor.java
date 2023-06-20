package io.dataease.api.permissions.user.dto;

import lombok.Data;

import java.io.Serial;

@Data
public class UserEditor extends UserCreator{

    @Serial
    private static final long serialVersionUID = 1580870660998152922L;

    private Long id;
}
