package io.dataease.api.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "在线地图构造器")
@Data
public class OnlineMapEditor implements Serializable {
    @Schema(description = "在线地图类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mapType;
    @Schema(description = "在线地图key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
    @Schema(description = "在线地图安全密钥", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String securityCode;
    @Schema(description = "自定义地图服务类型（raster/vector）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String serviceType;
    @Schema(description = "瓦片服务地址，最长 2048 个字符", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileUrl;
    @Schema(description = "矢量地图 Style JSON 地址，最长 2048 个字符", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String styleUrl;
    @Schema(description = "瓦片编号方案", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileScheme;
    @Schema(description = "瓦片尺寸", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileSize;
    @Schema(description = "自定义地图最小缩放层级，范围 0-24，最多 1 位小数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileMinZoom;
    @Schema(description = "自定义地图最大缩放层级，范围 0-24，最多 1 位小数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileMaxZoom;
    @Schema(description = "栅格瓦片服务版权信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileAttribution;
    @Schema(description = "矢量地图版权信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String styleAttribution;
    @Schema(description = "是否显示栅格瓦片版权信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tileAttributionEnabled;
    @Schema(description = "是否显示矢量地图版权信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String styleAttributionEnabled;
}
