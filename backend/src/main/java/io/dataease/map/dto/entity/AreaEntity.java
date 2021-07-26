package io.dataease.map.dto.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaEntity implements Serializable {

    private static final long serialVersionUID = -1326667005437020282L;

    private String code;

    private String name;

    private String pcode;

    private List<AreaEntity> children;

    public void addChild(AreaEntity entity) {
        children = Optional.ofNullable(children).orElse(new ArrayList<>());
        entity.setPcode(code);
        children.add(entity);
    }
}
