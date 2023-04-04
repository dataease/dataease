package io.dataease.xpack.permissions.org.bo;

import io.dataease.xpack.permissions.org.dao.auto.entity.PerOrg;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrgTreeNode extends PerOrg {

    private List<OrgTreeNode> children = new ArrayList<>();
}
