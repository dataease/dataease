package io.dataease.extensions.datasource.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class FieldGroupDTO {
    private String name;

    private List<String> text;

    private String min;

    private String minTerm;

    private String max;

    private String maxTerm;

    private String startTime;

    private String endTime;
}
