package io.datains.controller.portal;


import io.datains.base.domain.PortalData;
import io.datains.commons.utils.AuthUtils;
import io.datains.dto.panel.PanelViewTableDTO;
import io.datains.service.panel.PanelViewService;
import io.datains.service.portal.PortalDataService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据门户 前端控制器
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-06-02
 */
@RestController
@RequestMapping("portal/data")
public class PortalDataController {

    @Resource
    private PortalDataService portalDataService;

    @ApiOperation("数据门户列表")
    @GetMapping("/list/{pageNum}/{pageSize}")
    public Map<String,Object> list( @PathVariable("pageNum") Integer pageNum,@PathVariable("pageSize") Integer pageSize){
        Long userId = AuthUtils.getUser().getUserId();
        return portalDataService.list(userId,pageNum,pageSize);
    }

    @ApiOperation("添加数据门户")
    @PostMapping("/save")
    public void save(@RequestBody PortalData portalData){
        portalData.setUserId(AuthUtils.getUser().getUserId());
        portalData.setUserName(AuthUtils.getUser().getUsername());
        portalDataService.save(portalData);
    }

    @ApiOperation("删除数据门户")
    @PostMapping("/delete/{id}")
    public void del(@PathVariable("id") Integer id){
        portalDataService.del(id);
    }

    @ApiOperation("修改数据门户")
    @PostMapping("/update")
    public void update(@RequestBody PortalData portalData){
        portalData.setUpdateBy(AuthUtils.getUser().getUsername());
        portalData.setUpdateTime(LocalDateTime.now());
        portalDataService.update(portalData);
    }


}

