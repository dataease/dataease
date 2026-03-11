package io.dataease.api.report.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TableSysVariable implements Serializable {

    private Long tableId;

    private List<String> sysVariables;
}
