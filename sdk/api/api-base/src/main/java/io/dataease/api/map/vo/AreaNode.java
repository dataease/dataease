package io.dataease.api.map.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -2285934203102231711L;
    private String id;
    private String level;
    private String name;
    private String pid;
    /**
     * 国家代码
     */
    private String country;
    /**
     * 下属区域
     */
    private List<AreaNode> children;
}
