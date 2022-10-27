package io.dataease.controller.panel;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.sys.request.KeyGridRequest;
import io.dataease.dto.appTemplateMarket.AppLogGridDTO;
import io.dataease.service.panel.applog.AppLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(tags = "应用市场：应用日志")
@ApiSupport(order = 220)
@RequestMapping("app/log")
public class AppLogController {

    @Resource
    private AppLogService appLogService;

    @I18n
    @ApiOperation("查询日志")
    @PostMapping("/logGrid/{goPage}/{pageSize}")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "request", value = "查询条件", required = true)
    })
    public Pager<List<AppLogGridDTO>> logGrid(@PathVariable int goPage, @PathVariable int pageSize,
                                              @RequestBody KeyGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, appLogService.query(request));
    }


    @ApiOperation("删除日志和资源")
    @PostMapping("/deleteLog")
    public void deleteLogAndResource(@RequestBody AppLogGridDTO request) throws Exception {
        appLogService.deleteLogAndResource(request);
    }

}
