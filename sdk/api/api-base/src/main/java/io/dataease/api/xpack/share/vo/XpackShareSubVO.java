package io.dataease.api.xpack.share.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 子资源（分页组件内嵌画布）公共链接信息
 * <p>
 * 用于公共链接访问时，查询 tab 内嵌子画布是否已开启分享、
 * 以及是否能被无密码直接访问（前端据此决定用 iframe 加载还是显示提示）。
 */
@Schema(description = "子资源分享信息VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareSubVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否已开启公共链接")
    private boolean exist;

    @Schema(description = "分享 UUID，exist=true 且可直接访问时不为空")
    private String uuid;

    @Schema(description = "分享是否已过期")
    private boolean expired;

    @Schema(description = "是否需要密码（前端可据此提示）")
    private boolean pwdRequired;
}
