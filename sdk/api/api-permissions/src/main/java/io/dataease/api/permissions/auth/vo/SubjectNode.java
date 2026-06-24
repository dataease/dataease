package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubjectNode implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    private String name;

    private Integer type = 0;

    private Map<String, Object> attrs;

    private List<SubjectNode> children;

    public SubjectNode(Long id, Long pid, String name, Integer type) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.type = type;
    }
}
