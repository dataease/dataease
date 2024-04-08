package io.dataease.api.xpack.share;

import io.dataease.api.xpack.share.request.*;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.xpack.share.vo.XpackShareGridVO;
import io.dataease.api.xpack.share.vo.XpackShareProxyVO;
import io.dataease.api.xpack.share.vo.XpackShareVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "分享")
public interface XpackShareApi {

    @Operation(summary = "查询资源分享状态")
    @Parameter(name = "resourceId", description = "资源ID", required = true, in = ParameterIn.PATH)
    @GetMapping("/status/{resourceId}")
    boolean status(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "切换资源分享状态")
    @Parameter(name = "resourceId", description = "资源ID", required = true, in = ParameterIn.PATH)
    @PostMapping("/switcher/{resourceId}")
    void switcher(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "设置分享有效期")
    @PostMapping("/editExp")
    void editExp(@RequestBody XpackShareExpRequest request);

    @Operation(summary = "编辑分享密码")
    @PostMapping("/editPwd")
    void editPwd(@RequestBody XpackSharePwdRequest request);

    @Operation(summary = "查询分享详情")
    @GetMapping("/detail/{resourceId}")
    @Parameter(name = "resourceId", description = "资源ID", required = true, in = ParameterIn.PATH)
    XpackShareVO detail(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "查询分享列表")
    @PostMapping("/query")
    List<XpackShareGridVO> query(@RequestBody VisualizationWorkbranchQueryRequest request);

    @Operation(summary = "查询分享代理信息")
    @PostMapping("/proxyInfo")
    XpackShareProxyVO proxyInfo(@RequestBody XpackShareProxyRequest request);

    @Operation(summary = "验证分享")
    @PostMapping("/validate")
    boolean validatePwd(@RequestBody XpackSharePwdValidator validator);

    @Operation(summary = "", hidden = true)
    @GetMapping("/queryRelationByUserId/{uid}")
    Map<String, String> queryRelationByUserId(@PathVariable("uid") Long uid);

    @Operation(summary = "编辑分享uuid")
    @PostMapping("/editUuid")
    String editUuid(@RequestBody XpackShareUuidEditor editor);
}
