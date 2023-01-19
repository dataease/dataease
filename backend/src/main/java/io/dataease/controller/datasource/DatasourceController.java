package io.dataease.controller.datasource;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.dataease.auth.annotation.DeLog;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysAuthConstants;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datasource.request.UpdataDsRequest;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.datasource.DBTableDTO;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.DatasourceExample;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.provider.ProviderFactory;
import io.dataease.service.datasource.DatasourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.CREATE,
            sourcetype = SysLogConstants.SOURCE_TYPE.DATASOURCE,
            positionIndex = 0, positionKey = "type",
            value = "id"
    )
    public Datasource addDatasource(@RequestBody DatasourceDTO datasource) throws Exception {
        return datasourceService.addDatasource(datasource);
    }

    @RequiresPermissions("datasource:read")
    @DePermission(type = DePermissionType.DATASOURCE, value = "id", level = ResourceAuthLevel.DATASOURCE_LEVEL_MANAGE)
    @ApiOperation("更新数据源")
    @PostMapping("/update")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
            sourcetype = SysLogConstants.SOURCE_TYPE.DATASOURCE,
            positionIndex = 0, positionKey = "type",
            value = "id"
    )
    public void updateDatasource(@RequestBody UpdataDsRequest updataDsRequest) throws Exception {
        DatasourceDTO datasource = new DatasourceDTO();
        datasource.setConfigurationEncryption(updataDsRequest.isConfigurationEncryption());
        datasource.setName(updataDsRequest.getName());
        datasource.setDesc(updataDsRequest.getDesc());
        datasource.setConfiguration(updataDsRequest.getConfiguration());
        datasource.setCreateTime(null);
        datasource.setType(updataDsRequest.getType());
        datasource.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(updataDsRequest.getId())) {
            datasource.setId(updataDsRequest.getId());
        }
        datasourceService.preCheckDs(datasource);
        if (StringUtils.isNotEmpty(updataDsRequest.getId())) {
            datasourceService.updateDatasource(updataDsRequest.getId(), datasource);
        } else {
            datasourceService.insert(datasource);
        }
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("数据源类型")
    @GetMapping("/types")
    public Collection types() throws Exception {
        return datasourceService.types();
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

    @ApiOperation("查询数据源详情")
    @PostMapping("/get/{id}")
    public DatasourceDTO getDatasource(@PathVariable String id) throws Exception {
        return datasourceService.getDataSourceDetails(id);
    }

    @ApiOperation("查询当前用户数据源")
    @GetMapping("/list/{type}")
    public List<DatasourceDTO> getDatasourceListByType(@PathVariable String type) throws Exception {
        return getDatasourceList().stream().filter(datasourceDTO -> datasourceDTO.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
    }

    @RequiresPermissions("datasource:read")
    @DePermission(type = DePermissionType.DATASOURCE, level = ResourceAuthLevel.DATASOURCE_LEVEL_MANAGE, value = "id")
    @ApiOperation("删除数据源")
    @PostMapping("/delete/{datasourceID}")
    public ResultHolder deleteDatasource(@PathVariable(value = "datasourceID") String datasourceID) throws Exception {
        Datasource datasource = datasourceService.get(datasourceID);
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATASOURCE, datasourceID, datasource.getType(), null, null);
        ResultHolder resultHolder = datasourceService.deleteDatasource(datasourceID);
        if (ObjectUtils.isNotEmpty(resultHolder) && resultHolder.isSuccess())
            DeLogUtils.save(sysLogDTO);
        return resultHolder;
    }

    @DePermission(type = DePermissionType.DATASOURCE)
    @ApiOperation("查询数据源下属所有表")
    @PostMapping("/getTables/{id}")
    public List<DBTableDTO> getTables(@PathVariable String id) throws Exception {
        return datasourceService.getTables(id);
    }

    @ApiIgnore
    @PostMapping("/getSchema")
    public List<String> getSchema(@RequestBody DatasourceDTO datasource) throws Exception {
        return datasourceService.getSchema(datasource);
    }

    @ApiIgnore
    @PostMapping("/checkApiDatasource")
    public ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws Exception {
        ApiDefinition apiDefinition = new Gson().fromJson(new String(java.util.Base64.getDecoder().decode(data.get("data"))), new TypeToken<ApiDefinition>() {
        }.getType());
        if(data.keySet().contains("type") && data.get("type").equals("apiStructure")){
            apiDefinition.setShowApiStructure(true);
        }
        return datasourceService.checkApiDatasource(apiDefinition);
    }


}
