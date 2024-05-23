package io.dataease.api.visualization;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import static io.dataease.constant.AuthResourceEnum.PANEL;


@Tag(name = "可视化管理:订阅")
@ApiSupport(order = 994)
@DeApiPath(value = "/subscription", rt = PANEL)
public interface VisualizationSubscriptionApi {

    @PostMapping("/{dvId}/{resourceId}")
    @Operation(summary = "订阅")
    void subscription(@PathVariable("dvId") Long dvId, @PathVariable("resourceId") Long resourceId);
}
