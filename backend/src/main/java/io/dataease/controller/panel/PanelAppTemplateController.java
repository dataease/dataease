package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.panel.PanelAppTemplateRequest;
import io.dataease.plugins.common.base.domain.PanelAppTemplateWithBLOBs;
import io.dataease.service.panel.PanelAppTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Api(tags = "仪表板：应该关系")
@ApiSupport(order = 170)
@RestController
@RequestMapping("appTemplate")
public class PanelAppTemplateController {

    @Resource
    private PanelAppTemplateService panelAppTemplateService;

    @ApiOperation("查询")
    @PostMapping("/find")
    @I18n
    public List<PanelAppTemplateWithBLOBs> appTemplateList(@RequestBody PanelAppTemplateRequest request) {
        return panelAppTemplateService.list(request);
    }

    @ApiOperation("保存")
    @PostMapping("/save")
    @I18n
    public void save(@RequestBody PanelAppTemplateRequest request) {
        panelAppTemplateService.save(request);
    }

    @ApiOperation("更新")
    @PostMapping("/update")
    @I18n
    public void update(@RequestBody PanelAppTemplateRequest request) {
        panelAppTemplateService.update(request);
    }

    @ApiOperation("删除")
    @DeleteMapping("/delete/{appTemplateId}")
    @I18n
    public void delete(@PathVariable String appTemplateId) {
        panelAppTemplateService.delete(appTemplateId);
    }

    @ApiOperation("名称校验")
    @PostMapping("/nameCheck")
    @I18n
    public String nameCheck(@RequestBody PanelAppTemplateRequest request) {
            return panelAppTemplateService.nameCheck(request);
    }

}
