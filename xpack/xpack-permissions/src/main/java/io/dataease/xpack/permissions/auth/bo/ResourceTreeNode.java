package io.dataease.xpack.permissions.auth.bo;

import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResourceTreeNode extends BusiResourcePO implements Serializable {

    private List<ResourceTreeNode> children;
}
