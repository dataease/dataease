package io.dataease.map.bo;

import io.dataease.map.dao.auto.entity.Area;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AreaBO extends Area implements Serializable {
    private boolean custom = false;
}
