package io.dataease.controller.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.DatasetTableIncrementalConfig;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.service.dataset.DataSetTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @ApiOperation("批量保存")
    @PostMapping("batchAdd")
    public void batchAdd(@RequestBody List<DataSetTableRequest> datasetTable) throws Exception {
        dataSetTableService.batchInsert(datasetTable);
    }

    @ApiOperation("更新")
    @PostMapping("update")
    public DatasetTable save(@RequestBody DataSetTableRequest datasetTable) throws Exception {
        return dataSetTableService.save(datasetTable);
    }

    @ApiOperation("删除")
    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) throws Exception {
        dataSetTableService.delete(id);
    }

    @ApiOperation("查询")
    @PostMapping("list")
    public List<DataSetTableDTO> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.list(dataSetTableRequest);
    }

    @ApiOperation("查询组")
    @PostMapping("listAndGroup")
    public List<DataSetTableDTO> listAndGroup(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.listAndGroup(dataSetTableRequest);
    }

    @ApiOperation("详息")
    @PostMapping("get/{id}")
    public DatasetTable get(@PathVariable String id) {
        return dataSetTableService.get(id);
    }

    @ApiOperation("带权限查询")
    @PostMapping("getWithPermission/{id}")
    public DataSetTableDTO getWithPermission(@PathVariable String id) {
        return dataSetTableService.getWithPermission(id);
    }

    @ApiOperation("查询原始字段")
    @PostMapping("getFields")
    public List<TableFiled> getFields(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getFields(dataSetTableRequest);
    }

    @ApiOperation("查询生成字段")
    @PostMapping("getFieldsFromDE")
    public Map<String, List<DatasetTableField>> getFieldsFromDE(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getFieldsFromDE(dataSetTableRequest);
    }

    @ApiOperation("查询预览数据")
    @PostMapping("getPreviewData/{page}/{pageSize}")
    public Map<String, Object> getPreviewData(@RequestBody DataSetTableRequest dataSetTableRequest, @PathVariable Integer page, @PathVariable Integer pageSize) throws Exception {
        return dataSetTableService.getPreviewData(dataSetTableRequest, page, pageSize);
    }

    @ApiOperation("根据sql查询预览数据")
    @PostMapping("sqlPreview")
    public Map<String, Object> getSQLPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getSQLPreview(dataSetTableRequest);
    }

    @ApiOperation("客户预览数据")
    @PostMapping("customPreview")
    public Map<String, Object> customPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getCustomPreview(dataSetTableRequest);
    }

    @ApiOperation("查询增量配置")
    @PostMapping("incrementalConfig")
    public DatasetTableIncrementalConfig incrementalConfig(@RequestBody DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        return dataSetTableService.incrementalConfig(datasetTableIncrementalConfig);
    }

    @ApiOperation("保存增量配置")
    @PostMapping("save/incrementalConfig")
    public void saveIncrementalConfig(@RequestBody DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        dataSetTableService.saveIncrementalConfig(datasetTableIncrementalConfig);
    }

    @ApiOperation("数据集详息")
    @PostMapping("datasetDetail/{id}")
    public Map<String, Object> datasetDetail(@PathVariable String id) {
        return dataSetTableService.getDatasetDetail(id);
    }

    @ApiOperation("excel上传")
    @PostMapping("excel/upload")
    public Map<String, Object> excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("tableId") String tableId) throws Exception {
        return dataSetTableService.excelSaveAndParse(file, tableId);
    }

    @ApiOperation("检测doris")
    @PostMapping("checkDorisTableIsExists/{id}")
    public Boolean checkDorisTableIsExists(@PathVariable String id) throws Exception {
        return dataSetTableService.checkDorisTableIsExists(id);
    }

    @ApiOperation("搜索")
    @PostMapping("search")
    public List<DataSetTableDTO> search(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.search(dataSetTableRequest);
    }
}
