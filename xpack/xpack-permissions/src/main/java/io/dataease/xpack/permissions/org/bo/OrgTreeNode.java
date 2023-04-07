package io.dataease.xpack.permissions.org.bo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrgTreeNode extends PerOrgItem {

    private List<OrgTreeNode> children = new ArrayList<>();
}
