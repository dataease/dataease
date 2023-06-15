package io.dataease.xpack.permissions.auth.dao.ext.entity;

import io.dataease.model.TreeBaseModel;
import lombok.Data;

import java.util.Objects;

@Data
public class BusiPerPO implements TreeBaseModel {

    private Long id;
    private String name;
    private Boolean leaf;
    private Integer weight;

    private Long pid;
    private Integer extraFlag;
    private Long targetId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusiPerPO busiPerPO = (BusiPerPO) o;
        return Objects.equals(getId(), busiPerPO.getId());
    }

}
