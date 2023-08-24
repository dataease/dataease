package io.dataease.plugins.server;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.entity.XpackConditionEntity;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.auth.dto.request.DataSetColumnPermissionsDTO;
import io.dataease.plugins.xpack.auth.dto.request.DatasetColumnPermissions;
import io.dataease.plugins.xpack.auth.service.ColumnPermissionService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import java.util.ArrayList;
import java.util.List;
@ApiIgnore
@RestController
@RequestMapping("plugin/dataset/columnPermissions")
public class ColumnPermissionsController {

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("save")
    public DatasetColumnPermissions save(@RequestBody DatasetColumnPermissions datasetColumnPermissions) throws Exception {
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
        DataSetColumnPermissionsDTO request = new DataSetColumnPermissionsDTO();
        request.setAuthTargetType(datasetColumnPermissions.getAuthTargetType());
        request.setAuthTargetId(datasetColumnPermissions.getAuthTargetId());
        request.setDatasetId(datasetColumnPermissions.getDatasetId());
        List<DataSetColumnPermissionsDTO> columnPermissionsDTOS = columnPermissionService.searchPermissions(request);
        if(StringUtils.isEmpty(datasetColumnPermissions.getId())){
            if(!CollectionUtils.isEmpty(columnPermissionsDTOS)){
                throw new Exception(Translator.get("i18n_cp_exist"));
            }
        }else {
            if(!CollectionUtils.isEmpty(columnPermissionsDTOS) && columnPermissionsDTOS.size() > 1){
                throw new Exception(Translator.get("i18n_cp_exist"));
            }
            if(columnPermissionsDTOS.size() == 1 && !columnPermissionsDTOS.get(0).getId().equalsIgnoreCase(datasetColumnPermissions.getId())){
                throw new Exception(Translator.get("i18n_cp_exist"));
            }
        }
        return columnPermissionService.save(datasetColumnPermissions);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("查询")
    @PostMapping("/list")
    public List<DataSetColumnPermissionsDTO> searchPermissions(@RequestBody DataSetColumnPermissionsDTO request) {
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
       return columnPermissionService.searchPermissions(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("删除")
    @PostMapping("/delete")
    public void delete(@RequestBody DatasetColumnPermissions datasetColumnPermissions) {
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
        columnPermissionService.delete(datasetColumnPermissions.getId());
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("分页查询")
    @PostMapping("/pageList/{datasetId}/{goPage}/{pageSize}")
    public Pager<List<DataSetColumnPermissionsDTO>> rowPermissions(@PathVariable String datasetId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
        List<XpackConditionEntity> conditionEntities = request.getConditions() == null ? new ArrayList<>() : request.getConditions();
        XpackConditionEntity entity =  new XpackConditionEntity();
        entity.setField("dataset_column_permissions.dataset_id");
        entity.setOperator("eq");
        entity.setValue(datasetId);
        conditionEntities.add(entity);
        request.setConditions(conditionEntities);
        return PageUtils.setPageInfo(page, columnPermissionService.queryPermissions(request));
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("有权限的对象")
    @PostMapping("/authObjs")
    public List<Object> authObjs(@RequestBody DataSetColumnPermissionsDTO request) {
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
        return (List<Object>) columnPermissionService.authObjs(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("详情")
    @PostMapping("/permissionInfo")
    public DataSetColumnPermissionsDTO permissionInfo(@RequestBody DataSetColumnPermissionsDTO request) {
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
        return columnPermissionService.permissionInfo(request);
    }

}
