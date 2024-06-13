package io.dataease.api.chart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * @Author gin
 */
@Getter
@Setter
public class Series {
    private String name;
    private String type;
    private List<Object> data;
    private Set<String> categories;
}
