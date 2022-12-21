package io.dataease.controller.sys;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.dto.RelationDTO;
import io.dataease.service.sys.RelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WiSoniC
 * @date 2022年12月8日15:01:05
 */
@Api(tags = "系统：血缘关系")
@ApiSupport(order = 230)
@RequestMapping("/api/relation")
@RestController
public class RelationController {

    @Resource
    private RelationService relationService;

    @DePermission(type = DePermissionType.DATASOURCE, level = ResourceAuthLevel.DATASOURCE_LEVEL_USE,value = "datasourceId")
    @ApiOperation("获取指定数据库的血源关系")
    @GetMapping("/datasource/{datasourceId}")
    public List<RelationDTO> getRelationForDatasource(@PathVariable String datasourceId) {
        Long userId = AuthUtils.getUser().getUserId();
        return relationService.getRelationForDatasource(datasourceId, userId);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE,value = "datasetId")
    @ApiOperation("获取指定数据集的血缘关系")
    @GetMapping("/dataset/{datasetId}")
    public RelationDTO getRelationForDataset(@PathVariable String datasetId) {
        Long userId = AuthUtils.getUser().getUserId();
        return relationService.getRelationForDataset(datasetId, userId);
    }

    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_VIEW,value = "panelId")
    @ApiOperation("获取指定仪表板的血源关系")
    @GetMapping("/panel/{panelId}")
    public List<RelationDTO> getRelationForPanel(@PathVariable String panelId) {
        Long userId = AuthUtils.getUser().getUserId();
        return relationService.getRelationForPanel(panelId, userId);
    }
}
