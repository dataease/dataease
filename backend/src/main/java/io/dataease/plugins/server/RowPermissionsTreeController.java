package io.dataease.plugins.server;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.auth.service.RowPermissionTreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@ApiIgnore
@Api(tags = "xpack：行权限")
@RestController
@RequestMapping("plugin/dataset/rowPermissionsTree")
public class RowPermissionsTreeController {

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("save")
    public void save(@RequestBody DataSetRowPermissionsTreeDTO request) {
        if (StringUtils.isEmpty(request.getAuthTargetType())) {
            DEException.throwException(Translator.get("i18n_row_permission_type_error"));
        }
        if (!StringUtils.equalsIgnoreCase(request.getAuthTargetType(), "sysParams")) {
            if (ObjectUtils.isEmpty(request.getAuthTargetId())) {
                DEException.throwException(Translator.get("i18n_row_permission_id"));
            }
        }
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
    public List<DataSetRowPermissionsTreeDTO> getByDs(@RequestBody DatasetRowPermissionsTreeRequest request) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        return rowPermissionTreeService.list(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("根据数据集分页查找行权限")
    @PostMapping("getByDsPage/{goPage}/{pageSize}")
    public Pager<List<DataSetRowPermissionsTreeDTO>> getByDs(@RequestBody DatasetRowPermissionsTreeRequest request, @PathVariable int goPage, @PathVariable int pageSize) {
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<DataSetRowPermissionsTreeDTO> list = rowPermissionTreeService.list(request);
        Pager<List<DataSetRowPermissionsTreeDTO>> setPageInfo = PageUtils.setPageInfo(page, list);
        return setPageInfo;
    }
}
