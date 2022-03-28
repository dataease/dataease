package io.dataease.controller.datasource;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datasource.request.UpdataDsRequest;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.dto.datasource.DBTableDTO;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.dto.DatasourceDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "数据源：数据源管理")
@ApiSupport(order = 30)
@RequestMapping("datasource")
@RestController
public class DatasourceController {

    @Resource
    private DatasourceService datasourceService;

    @RequiresPermissions("datasource:read")
    @DePermission(type = DePermissionType.DATASOURCE, value = "id")
    @ApiOperation("新增数据源")
    @PostMapping("/add")
    public Datasource addDatasource(@RequestBody Datasource datasource) throws Exception{
        return datasourceService.addDatasource(datasource);
    }

    @ApiIgnore
    @PostMapping("/validate")
    public ResultHolder validate(@RequestBody DatasourceDTO datasource) throws Exception {
        return datasourceService.validate(datasource);
    }

    @RequiresPermissions("datasource:read")
    @DePermission(type = DePermissionType.DATASOURCE, value = "id")
    @ApiOperation("验证数据源")
    @GetMapping("/validate/{datasourceId}")
    public ResultHolder validate(@PathVariable String datasourceId) {
        return datasourceService.validate(datasourceId);
    }

    @ApiOperation("查询当前用户数据源")
    @GetMapping("/list")
    public List<DatasourceDTO> getDatasourceList() throws Exception {
        DatasourceUnionRequest request = new DatasourceUnionRequest();
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        return datasourceService.getDatasourceList(request);
    }

    @ApiOperation("查询当前用户数据源")
    @GetMapping("/list/{type}")
    public List<DatasourceDTO> getDatasourceListByType(@PathVariable String type) throws Exception {
        return getDatasourceList().stream().filter(datasourceDTO -> datasourceDTO.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
    }

    @RequiresPermissions("datasource:read")
    @DePermission(type = DePermissionType.DATASOURCE, level = ResourceAuthLevel.DATASOURCE_LEVEL_MANAGE)
    @ApiOperation("删除数据源")
    @PostMapping("/delete/{datasourceID}")
    public ResultHolder deleteDatasource(@PathVariable(value = "datasourceID") String datasourceID) throws Exception {
        return datasourceService.deleteDatasource(datasourceID);
    }

    @RequiresPermissions("datasource:read")
    @DePermission(type = DePermissionType.DATASOURCE, value = "id", level = ResourceAuthLevel.DATASOURCE_LEVEL_MANAGE)
    @ApiOperation("更新数据源")
    @PostMapping("/update")
    public void updateDatasource(@RequestBody UpdataDsRequest dsRequest) throws Exception{
        datasourceService.updateDatasource(dsRequest);
    }

    @DePermission(type = DePermissionType.DATASOURCE)
    @ApiOperation("查询数据源下属所有表")
    @PostMapping("/getTables/{id}")
    public List<DBTableDTO> getTables(@PathVariable String id) throws Exception {
        return datasourceService.getTables(id);
    }

    @ApiIgnore
    @PostMapping("/getSchema")
    public List<String> getSchema(@RequestBody Datasource datasource) throws Exception {
       return datasourceService.getSchema(datasource);
    }

    @ApiIgnore
    @PostMapping("/checkApiDatasource")
    public ApiDefinition checkApiDatasource(@RequestBody ApiDefinition apiDefinition) throws Exception {
        return datasourceService.checkApiDatasource(apiDefinition);
    }


}
