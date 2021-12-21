package io.dataease.plugins.server;

import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.auth.dto.request.DataSetRowPermissionsDTO;
import io.dataease.plugins.xpack.auth.dto.request.DatasetRowPermissions;
import io.dataease.plugins.xpack.auth.dto.response.XpackSysAuthDetailDTO;
import io.dataease.plugins.xpack.auth.service.RowPermissionService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("plugin/dataset/rowPermissions")
public class RowPermissionsController {

    @ApiOperation("保存")
    @PostMapping("save")
    public void save(@RequestBody DatasetRowPermissions datasetRowPermissions) throws Exception {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        rowPermissionService.save(datasetRowPermissions);
    }

    @ApiOperation("查询")
    @PostMapping("/list")
    public List<DataSetRowPermissionsDTO> rowPermissions(@RequestBody XpackSysAuthDetailDTO request) {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
       return rowPermissionService.searchRowPermissions(request);
    }

    @ApiOperation("删除")
    @GetMapping("/delete/{id}")
    public void dataSetRowPermissionInfo(@PathVariable String id) {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        rowPermissionService.delete(id);
    }

}
