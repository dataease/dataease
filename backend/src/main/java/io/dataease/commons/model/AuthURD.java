package io.dataease.commons.model;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AuthURD implements Serializable {

    private List<Long> userIds;

    private List<Long> deptIds;

    private List<Long> roleIds;


}
