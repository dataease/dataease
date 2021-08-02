package io.dataease.controller.sys.response;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SettingTreeNode implements Serializable {

    private static final long serialVersionUID = -2416283978185545199L;

    private Long id;

    private String name;

    private List<SettingTreeNode> children;
}
