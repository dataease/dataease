package io.datains.plugins.server;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.datains.auth.annotation.DePermission;
import io.datains.commons.constants.DePermissionType;
import io.datains.commons.constants.ResourceAuthLevel;
import io.datains.commons.utils.PageUtils;
import io.datains.commons.utils.Pager;
import io.datains.i18n.Translator;
import io.datains.plugins.common.entity.XpackConditionEntity;
import io.datains.plugins.common.entity.XpackGridRequest;
import io.datains.plugins.config.SpringContextUtil;
import io.datains.plugins.xpack.auth.dto.request.DataSetRowPermissionsDTO;
import io.datains.plugins.xpack.auth.dto.request.DatasetRowPermissions;
import io.datains.plugins.xpack.auth.service.RowPermissionService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
@ApiIgnore
@RestController
@RequestMapping("plugin/dataset/rowPermissions")
public class RowPermissionsController {

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("save")
    public void save(@RequestBody DatasetRowPermissions datasetRowPermissions) throws Exception {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);

        DataSetRowPermissionsDTO request = new DataSetRowPermissionsDTO();
        request.setAuthTargetType(datasetRowPermissions.getAuthTargetType());
        request.setAuthTargetId(datasetRowPermissions.getAuthTargetId());
        request.setDatasetFieldId(datasetRowPermissions.getDatasetFieldId());
        List<DataSetRowPermissionsDTO> rowPermissionsDTOS = rowPermissionService.searchRowPermissions(request);
        if(StringUtils.isEmpty(datasetRowPermissions.getId())){
            if(!CollectionUtils.isEmpty(rowPermissionsDTOS)){
                throw new Exception(Translator.get("i18n_rp_exist"));
            }
        }else {
            if(!CollectionUtils.isEmpty(rowPermissionsDTOS) && rowPermissionsDTOS.size() > 1){
                throw new Exception(Translator.get("i18n_rp_exist"));
            }
            if(rowPermissionsDTOS.size() == 1 && !rowPermissionsDTOS.get(0).getId().equalsIgnoreCase(datasetRowPermissions.getId())){
                throw new Exception(Translator.get("i18n_rp_exist"));
            }
        }
        rowPermissionService.save(datasetRowPermissions);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("查询")
    @PostMapping("/list")
    public List<DataSetRowPermissionsDTO> rowPermissions(@RequestBody DataSetRowPermissionsDTO request) {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
       return rowPermissionService.searchRowPermissions(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("删除")
    @PostMapping("/delete")
    public void dataSetRowPermissionInfo(@RequestBody DatasetRowPermissions datasetRowPermissions) {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        rowPermissionService.delete(datasetRowPermissions.getId());
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("分页查询")
    @PostMapping("/pageList/{datasetId}/{goPage}/{pageSize}")
    public Pager<List<DataSetRowPermissionsDTO>> rowPermissions(@PathVariable String datasetId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        List<XpackConditionEntity> conditionEntities = request.getConditions() == null ? new ArrayList<>() : request.getConditions();
        XpackConditionEntity entity = new XpackConditionEntity();
        entity.setField("dataset_row_permissions.dataset_id");
        entity.setOperator("eq");
        entity.setValue(datasetId);
        conditionEntities.add(entity);
        request.setConditions(conditionEntities);
        return PageUtils.setPageInfo(page, rowPermissionService.queryRowPermissions(request));
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("有权限的对象")
    @PostMapping("/authObjs")
    public List<Object> authObjs(@RequestBody DataSetRowPermissionsDTO request) {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        return (List<Object>) rowPermissionService.authObjs(request);
    }

    @DePermission(type = DePermissionType.DATASET, value = "datasetId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("详情")
    @PostMapping("/dataSetRowPermissionInfo")
    public DataSetRowPermissionsDTO dataSetRowPermissionInfo(@RequestBody DataSetRowPermissionsDTO request) {
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        return rowPermissionService.dataSetRowPermissionInfo(request);
    }

}
