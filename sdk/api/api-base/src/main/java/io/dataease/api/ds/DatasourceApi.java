package io.dataease.api.ds;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
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
    List<DatasourceDTO> query(@PathVariable("keyWord") String keyWord);

    @PostMapping("/save")
    DatasourceDTO save(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/update")
    DatasourceDTO update(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;


    @PostMapping("/move")
    DatasourceDTO move(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/reName")
    DatasourceDTO reName(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/createFolder")
    DatasourceDTO createFolder(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/checkRepeat")
    boolean checkRepeat(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @GetMapping("/types")
    List<DatasourceConfiguration.DatasourceType> datasourceTypes() throws DEException;

    @PostMapping("/validate")
    DatasourceDTO validate(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/getSchema")
    List<String> getSchema(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/delete/{datasourceId}")
    void delete(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @DePermit({"#p0+':read'"})
    @GetMapping("/get/{datasourceId}")
    DatasourceDTO get(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @PostMapping("/getTableField")
    List<TableField> getTableField(@RequestBody Map<String, String> req) throws DEException;

    @PostMapping("/syncApiTable")
    void syncApiTable(@RequestBody Map<String, String> req) throws DEException;

    @PostMapping("/syncApiDs")
    void syncApiDs(@RequestBody Map<String, String> req) throws Exception;

    @PostMapping("tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request) throws DEException;


    @DePermit({"#p0.datasourceId+':read'"})
    @PostMapping("getTables")
    List<DatasetTableDTO> getTables(@RequestBody DatasetTableDTO datasetTableDTO) throws DEException;

    @PostMapping("/checkApiDatasource")
    ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws DEException;

    @PostMapping("/uploadFile")
    ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws DEException;

    @PostMapping("/previewData")
    Map<String, Object> previewDataWithLimit(@RequestBody Map<String, Object> req) throws DEException;

    @PostMapping("/latestUse")
    public List<String> latestUse();

    @GetMapping("showFinishPage")
    public boolean showFinishPage() throws DEException;

    @PostMapping("setShowFinishPage")
    public void setShowFinishPage() throws DEException;

    @PostMapping("/listSyncRecord/{dsId}/{goPage}/{pageSize}")
    IPage<CoreDatasourceTaskLogDTO> listSyncRecord(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @PathVariable("dsId") Long dsId);
}
