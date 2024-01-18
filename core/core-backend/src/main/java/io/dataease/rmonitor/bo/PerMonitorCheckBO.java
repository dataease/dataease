package io.dataease.rmonitor.bo;

import lombok.Data;
import java.io.Serializable;

@Data
public class PerMonitorCheckBO implements Serializable {

    private boolean valid;

    private boolean emptyPermission;
}
