package io.dataease.api.permissions.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserImportVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3371025717928287780L;

    private String dataKey;

    private int successCount;

    private int errorCount;

    public UserImportVO(String dataKey) {
        this.dataKey = dataKey;
    }
}
