package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.PanelSubject;
import io.dataease.controller.request.panel.PanelSubjectRequest;
import io.dataease.service.panel.PanelSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-06
 * Description:
 */
@Api(tags = "仪表板：主题")
@ApiSupport(order = 160)
@RestController
@RequestMapping("panel/subject")
public class PanelSubjectController {

    @Resource
    private PanelSubjectService panelSubjectService;

    @ApiOperation("查询")
    @PostMapping("/query")
    public List<PanelSubject> query(@RequestBody PanelSubjectRequest request) {
        return panelSubjectService.query(request);
    }

    @ApiOperation("根据仪表板查询")
    @PostMapping("/querySubjectWithGroup")
    public List<PanelSubject> querySubjectWithGroup(@RequestBody PanelSubjectRequest request) {
        return panelSubjectService.querySubjectWithGroup(request);
    }

    @ApiOperation("更新")
    @PostMapping("/update")
    public void update(@RequestBody PanelSubjectRequest request) {
        panelSubjectService.update(request);
    }

    @ApiOperation("删除")
    @DeleteMapping("/delete/{id}")
    public void update(@PathVariable String id) {
        panelSubjectService.delete(id);
    }


}
