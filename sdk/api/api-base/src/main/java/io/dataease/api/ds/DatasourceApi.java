package io.dataease.api.ds;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.ds.vo.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@Tag(name = "数据源管理:基础")
@ApiSupport(order = 969)
@DeApiPath(value = "/datasource", rt = DATASOURCE)
public interface DatasourceApi {
    /**
     * 查询数据源树
     *
     * @param keyWord 过滤关键字
     * @return
     */
    @GetMapping("/query/{keyWord}")
    @Operation(summary = "查询")
    List<DatasourceDTO> query(@PathVariable("keyWord") String keyWord);

    @PostMapping("/save")
    @Operation(summary = "保存")
    DatasourceDTO save(@RequestBody BusiDsRequest dataSourceDTO) throws DEException;

    @PostMapping("/update")
    @Operation(summary = "更新")
    DatasourceDTO update(@RequestBody BusiDsRequest dataSourceDTO) throws DEException;

    @PostMapping("/move")
    @Operation(summary = "移动")
    DatasourceDTO move(@RequestBody BusiCreateFolderRequest dataSourceDTO) throws DEException;

    @PostMapping("/reName")
    @Operation(summary = "重命名")
    DatasourceDTO reName(@RequestBody BusiRenameRequest dataSourceDTO) throws DEException;

    @PostMapping("/createFolder")
    @Operation(summary = "新建文件夹")
    DatasourceDTO createFolder(@RequestBody BusiCreateFolderRequest dataSourceDTO) throws DEException;

    @PostMapping("/checkRepeat")
    @Operation(summary = "校验重复")
    boolean checkRepeat(@RequestBody BusiDsRequest dataSourceDTO) throws DEException;

    @GetMapping("/types")
    @Operation(summary = "数据源类型")
    List<DatasourceConfiguration.DatasourceType> datasourceTypes() throws DEException;

    @PostMapping("/validate")
    @Operation(summary = "校验")
    DatasourceDTO validate(@RequestBody BusiDsRequest dataSourceDTO) throws DEException;

    @PostMapping("/getSchema")
    @Operation(summary = "获取 schema")
    List<String> getSchema(@RequestBody BusiDsRequest dataSourceDTO) throws DEException;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/validate/{datasourceId}")
    @Operation(summary = "校验")
    DatasourceDTO validate(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @DePermit({"#p0+':manage'"})
    @PostMapping("/perDelete/{datasourceId}")
    @Operation(summary = "是否有数据集正在使用此数据源")
    boolean perDelete(@PathVariable("datasourceId") Long datasourceId);

    @DePermit({"#p0+':manage'"})
    @GetMapping("/delete/{datasourceId}")
    @Operation(summary = "删除")
    void delete(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @DePermit({"#p0+':read'"})
    @GetMapping("/get/{datasourceId}")
    @Operation(summary = "数据源详情")
    DatasourceDTO get(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @DePermit({"#p0+':read'"})
    @GetMapping("/hidePw/{datasourceId}")
    @Operation(summary = "数据源详情")
    DatasourceDTO hidePw(@PathVariable("datasourceId") Long datasourceId) throws DEException;


    @PostMapping("/getTableField")
    @Operation(summary = "获取表字段")
    List<TableField> getTableField(@RequestBody Map<String, String> req) throws DEException;

    @PostMapping("/syncApiTable")
    @Operation(summary = "同步API数据表")
    void syncApiTable(@RequestBody Map<String, String> req) throws DEException;

    @PostMapping("/syncApiDs")
    @Operation(summary = "同步API数据源")
    void syncApiDs(@RequestBody Map<String, String> req) throws Exception;

    @PostMapping("tree")
    @Operation(summary = "数据源列表")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request) throws DEException;


    @DePermit({"#p0.datasourceId+':read'"})
    @PostMapping("getTables")
    @Operation(summary = "获取表")
    List<DatasetTableDTO> getTables(@RequestBody DatasetTableDTO datasetTableDTO) throws DEException;

    @PostMapping("/checkApiDatasource")
    @Operation(summary = "校验API数据源")
    ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws DEException;

    @PostMapping("/uploadFile")
    @Operation(summary = "上传文件")
    ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws DEException;

    @PostMapping("/previewData")
    @Operation(summary = "预览数据")
    Map<String, Object> previewDataWithLimit(@RequestBody Map<String, Object> req) throws DEException;

    @PostMapping("/latestUse")
    @Operation(summary = "最近常用")
    public List<String> latestUse();

    @GetMapping("showFinishPage")
    @Operation(summary = "是否显示完成页面")
    public boolean showFinishPage() throws DEException;

    @PostMapping("setShowFinishPage")
    @Operation(summary = "是否显示完成页面")
    public void setShowFinishPage() throws DEException;

    @PostMapping("/listSyncRecord/{dsId}/{goPage}/{pageSize}")
    @Operation(summary = "更新日志")
    IPage<CoreDatasourceTaskLogDTO> listSyncRecord(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @PathVariable("dsId") Long dsId);

    DatasourceDTO innerGet(Long datasourceId) throws DEException;

    List<DatasourceDTO> innerList(List<Long> ids, List<String> types) throws DEException;

}
