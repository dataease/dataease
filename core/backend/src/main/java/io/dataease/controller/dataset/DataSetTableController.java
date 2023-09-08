package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DeLog;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.dataset.DataSetExportRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.response.DataSetDetail;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.dataset.ExcelFileData;
import io.dataease.plugins.common.base.domain.DatasetSqlLog;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.DatasetTableIncrementalConfig;
import io.dataease.plugins.common.dto.dataset.SqlVariableDetails;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.dataset.DataSetTableService;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/20 8:29 下午
 */
@Api(tags = "数据集：数据集表")
@ApiSupport(order = 50)
@RestController
@RequestMapping("dataset/table")
public class DataSetTableController {
    @Resource
    private DataSetTableService dataSetTableService;

    @Resource
    private VAuthModelService vAuthModelService;

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id"),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @ApiOperation("批量保存")
    @PostMapping("batchAdd")
    public List<VAuthModelDTO> batchAdd(@RequestBody List<DataSetTableRequest> datasetTable) throws Exception {
        List<String> ids = dataSetTableService.batchInsert(datasetTable).stream().map(DatasetTable::getId).collect(Collectors.toList());
        return vAuthModelService.queryAuthModelByIds("dataset", ids);
    }

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    @ApiOperation("更新")
    @PostMapping("update")
    public List<VAuthModelDTO> save(@RequestBody DataSetTableRequest datasetTable) throws Exception {
        if (datasetTable.getType().equalsIgnoreCase("excel")) {
            List<String> ids = dataSetTableService.saveExcel(datasetTable).stream().map(DatasetTable::getId).collect(Collectors.toList());
            return vAuthModelService.queryAuthModelByIds("dataset", ids);
        } else {
            return vAuthModelService.queryAuthModelByIds("dataset", Collections.singletonList(dataSetTableService.save(datasetTable).getId()));
        }
    }

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @ApiOperation("修改")
    @PostMapping("alter")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
            sourcetype = SysLogConstants.SOURCE_TYPE.DATASET,
            value = "id",
            positionIndex = 0,
            positionKey = "sceneId"
    )
    public void alter(@RequestBody DataSetTableRequest request) throws Exception {
        dataSetTableService.alter(request);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("删除")
    @PostMapping("delete/{id}")
    public void delete(@ApiParam(name = "id", value = "数据集ID", required = true) @PathVariable String id) throws Exception {
        dataSetTableService.delete(id);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "sceneId")
    @ApiOperation("查询")
    @PostMapping("list")
    public List<DataSetTableDTO> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.list(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "sceneId")
    @ApiOperation("查询组")
    @PostMapping("listAndGroup")
    public List<DataSetTableDTO> listAndGroup(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.listAndGroup(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE)
    @ApiOperation("详细信息")
    @PostMapping("get/{id}")
    public DatasetTable get(@ApiParam(name = "id", value = "数据集ID", required = true) @PathVariable String id) {
        return dataSetTableService.get(id);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE)
    @ApiOperation("带权限查询")
    @PostMapping("getWithPermission/{id}")
    public DataSetTableDTO getWithPermission(@PathVariable String id) {
        return dataSetTableService.getWithPermission(id, null);
    }

    @ApiOperation("查询原始字段")
    @PostMapping("getFields")
    public List<TableField> getFields(@RequestBody DatasetTable datasetTable) throws Exception {
        return dataSetTableService.getFields(datasetTable);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "id")
    @ApiOperation("查询生成字段")
    @PostMapping("getFieldsFromDE")
    public Map<String, List<DatasetTableField>> getFieldsFromDE(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getFieldsFromDE(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "id")
    @ApiOperation("查询预览数据")
    @PostMapping("getPreviewData/{page}/{pageSize}")
    public Map<String, Object> getPreviewData(@RequestBody DataSetTableRequest dataSetTableRequest, @PathVariable Integer page, @PathVariable Integer pageSize) throws Exception {
        return dataSetTableService.getPreviewData(dataSetTableRequest, page, pageSize, null, null);
    }

    @ApiOperation("db数据库表预览数据")
    @PostMapping("dbPreview")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_USE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    public Map<String, Object> getDBPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getDBPreview(dataSetTableRequest);
    }

    @ApiOperation("根据sql查询预览数据")
    @PostMapping("sqlPreview")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_USE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    public ResultHolder getSQLPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getSQLPreview(dataSetTableRequest, true);
    }

    @ApiOperation("根据sql查询预览数据")
    @PostMapping("sqlLog/{goPage}/{pageSize}")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_USE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    public Pager<List<DatasetSqlLog>> getSQLLog(@RequestBody DataSetTableRequest dataSetTableRequest, @PathVariable int goPage, @PathVariable int pageSize) throws Exception {
        Page<DatasetSqlLog> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableService.getSQLLog(dataSetTableRequest));
    }

    @ApiOperation("预览自定义数据数据")
    @PostMapping("customPreview")
    public Map<String, Object> customPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getCustomPreview(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "tableId")
    @ApiOperation("查询增量配置")
    @PostMapping("incrementalConfig")
    public DatasetTableIncrementalConfig incrementalConfig(@RequestBody DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        return dataSetTableService.incrementalConfig(datasetTableIncrementalConfig);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE, value = "tableId")
    @ApiOperation("保存增量配置")
    @PostMapping("save/incrementalConfig")
    public void saveIncrementalConfig(@RequestBody DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        dataSetTableService.saveIncrementalConfig(datasetTableIncrementalConfig);
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("数据集详细信息")
    @PostMapping("datasetDetail/{id}")
    public DataSetDetail datasetDetail(@PathVariable String id) {
        return dataSetTableService.getDatasetDetail(id);
    }

    @ApiOperation("excel上传")
    @PostMapping("excel/upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "tableId", value = "数据表ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "editType", value = "编辑类型", required = true, dataType = "Integer")
    })
    public ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("tableId") String tableId, @RequestParam("editType") Integer editType) throws Exception {
        return dataSetTableService.excelSaveAndParse(file, tableId, editType);
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("检测doris")
    @PostMapping("checkDorisTableIsExists/{id}")
    public Boolean checkDorisTableIsExists(@PathVariable String id) throws Exception {
        return dataSetTableService.checkEngineTableIsExists(id);
    }

    @ApiOperation("搜索")
    @PostMapping("search")
    public List<DataSetTableDTO> search(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.search(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("数据集同步表结构")
    @PostMapping("syncField/{id}")
    public DatasetTable syncDatasetTableField(@PathVariable String id) throws Exception {
        return dataSetTableService.syncDatasetTableField(id);
    }

    @DePermission(type = DePermissionType.DATASET, value = "id")
    @ApiOperation("关联数据集预览数据")
    @PostMapping("unionPreview")
    public Map<String, Object> unionPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getUnionPreview(dataSetTableRequest);
    }

    @ApiOperation("根据仪表板视图ID查询数据集变量")
    @PostMapping("/paramsWithIds/{type}")
    List<SqlVariableDetails> paramsWithIds(@PathVariable String type, @RequestBody List<String> viewIds) {
        return dataSetTableService.paramsWithIds(type, viewIds);
    }

    @ApiOperation("数据集的SQL变量")
    @PostMapping("/params/{id}/{type}")
    List<SqlVariableDetails> paramsWithIds(@PathVariable String type, @PathVariable String id) {
        return dataSetTableService.datasetParams(type, id);
    }

    @ApiOperation("根据数据集文件夹ID查询数据集名称")
    @PostMapping("/getDatasetNameFromGroup/{sceneId}")
    public List<String> getDatasetNameFromGroup(@PathVariable String sceneId) {
        return dataSetTableService.getDatasetNameFromGroup(sceneId);
    }

    @ApiOperation("数据集导出")
    @PostMapping("/exportDataset")
    @I18n
    public void exportDataset(@RequestBody DataSetExportRequest request, HttpServletResponse response) throws Exception {
        dataSetTableService.exportDataset(request, response);
    }
}
