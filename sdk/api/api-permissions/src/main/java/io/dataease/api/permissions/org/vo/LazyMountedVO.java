package io.dataease.api.permissions.org.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LazyMountedVO implements Serializable {

    private List<MountedVO> nodes;

    private String name;

    private List<String> expandKeyList;
}
