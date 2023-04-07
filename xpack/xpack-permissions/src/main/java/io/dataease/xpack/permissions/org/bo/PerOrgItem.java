package io.dataease.xpack.permissions.org.bo;

import io.dataease.xpack.permissions.org.dao.auto.entity.PerOrg;
import lombok.Data;

@Data
public class PerOrgItem extends PerOrg {

    private boolean disabled = true;
}
