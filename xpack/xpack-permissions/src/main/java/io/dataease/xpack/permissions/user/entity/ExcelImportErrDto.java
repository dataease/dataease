package io.dataease.xpack.permissions.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportErrDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -4676716054024817478L;

    private Object object;

    private Map<Integer, String> cellMap = null;
}
