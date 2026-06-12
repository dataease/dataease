package io.dataease.constant;

import lombok.Getter;

@Getter
public enum SubjectTypeEnum {
    USER(0), ROLE(1), ORG(2);

    private final int code;

    SubjectTypeEnum(int code) {
        this.code = code;
    }

}
