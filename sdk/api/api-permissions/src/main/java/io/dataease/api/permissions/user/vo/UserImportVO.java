package io.dataease.api.permissions.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "批量导入结果")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserImportVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3371025717928287780L;

    @Schema(description = "数据标志")
    private String dataKey;
    @Schema(description = "成功数量")
    private int successCount;
    @Schema(description = "失败数量")
    private int errorCount;

    public UserImportVO(String dataKey) {
        this.dataKey = dataKey;
    }
}
