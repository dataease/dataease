package io.dataease.plugins.server;

import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.auth.service.RowPermissionTreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@ApiIgnore
@Api(tags = "行权限")
@RestController
@RequestMapping("plugin/dataset/rowPermissionsTree")
public class RowPermissionsTreeController {

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("save")
    public void save(@RequestBody DataSetRowPermissionsTreeDTO request) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        rowPermissionTreeService.save(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("删除")
    @PostMapping("delete")
    public void delete(@RequestBody DataSetRowPermissionsTreeDTO request) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        rowPermissionTreeService.delete(request.getId());
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("根据ID查找行权限")
    @PostMapping("getById")
    public DataSetRowPermissionsTreeDTO getById(@RequestBody DataSetRowPermissionsTreeDTO request) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        return rowPermissionTreeService.get(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("根据数据集、当前组织/角色/用户查找行权限")
    @PostMapping("get")
    public DataSetRowPermissionsTreeDTO getBy(@RequestBody DataSetRowPermissionsTreeDTO request) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        return rowPermissionTreeService.get(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("根据数据集查找行权限")
    @PostMapping("getByDs")
    public List<DataSetRowPermissionsTreeDTO> getByDs(@RequestBody DataSetRowPermissionsTreeDTO request) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        return rowPermissionTreeService.list(request);
    }
}
