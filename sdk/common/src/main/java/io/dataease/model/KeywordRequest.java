package io.dataease.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class KeywordRequest implements Serializable {

    private String keyword;
}
