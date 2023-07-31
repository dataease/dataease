package io.dataease.api.ds;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;


@DeApiPath(value = "/datasource", rt = DATASOURCE)
public interface DatasourceApi {
    /**
     * 查询数据源树
     *
     * @param keyWord 过滤关键字
     * @return
     */
    @DePermit("m:read")
    @GetMapping("/query/{keyWord}")
    List<DatasourceDTO> query(@PathVariable("keyWord") String keyWord);

    @DePermit("m:read")
    @PostMapping("/save")
    DatasourceDTO save(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @DePermit({"m:read", "#p0.id+':manage'"})
    @PostMapping("/update")
    DatasourceDTO update(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @GetMapping("/types")
    List<DatasourceConfiguration.DatasourceType> datasourceTypes() throws DEException;

    @PostMapping("/validate")
    DatasourceDTO validate(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/getSchema")
    List<String> getSchema(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @DePermit({"m:read", "#p0+':manage'"})
    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @DePermit({"m:read", "#p0+':manage'"})
    @GetMapping("/delete/{datasourceId}")
    void delete(@PathVariable("datasourceId") Long datasourceId) throws Exception;

    @GetMapping("getTableField/{datasourceId}/{tableName}")
    List<TableField> getTableField(@PathVariable("datasourceId") String datasourceId, @PathVariable("tableName") String tableName) throws Exception;


    @DePermit("m:read")
    @PostMapping("tree")
    List<BusiNodeVO> tree(BusiNodeRequest request) throws DEException;


    @DePermit({"m:read", "#p0+':manage'"})
    @GetMapping("getTables/{datasourceId}")
    List<DatasetTableDTO> getTables(@PathVariable("datasourceId") String datasourceId) throws Exception;

    @PostMapping("/checkApiDatasource")
    ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws DEException;

    @PostMapping("/uploadFile")
    ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws Exception;

}
