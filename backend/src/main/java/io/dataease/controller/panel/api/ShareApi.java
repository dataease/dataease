package io.dataease.controller.panel.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.PanelShare;
import io.dataease.controller.request.panel.PanelShareFineDto;
import io.dataease.controller.request.panel.PanelShareRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelShareDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 分享API
 */
@Api(tags = "仪表板：分享管理")
@ApiSupport(order = 180)
@RequestMapping("/api/share")
public interface ShareApi {

    @ApiIgnore
    @ApiOperation("创建分享")
    @PostMapping("/")
    void share(PanelShareRequest request);

    @ApiOperation("查询分享")
    @PostMapping("/treeList")
    List<PanelShareDto> treeList(BaseGridRequest request);


    @ApiOperation("根据资源查询分享")
    @PostMapping("/queryWithResourceId")
    List<PanelShare> queryWithResourceId(BaseGridRequest request);


    @ApiOperation("优化创建分享")
    @PostMapping("/fineSave")
    void fineSave(PanelShareFineDto panelShareFineDto);
    
}
