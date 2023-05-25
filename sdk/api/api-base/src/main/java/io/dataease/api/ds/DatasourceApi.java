package io.dataease.api.ds;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.ApiDefinition;
import io.dataease.api.ds.vo.DatasourceConfiguration;

import io.dataease.api.ds.vo.TableField;
import io.dataease.auth.DeApiPath;
import io.dataease.exception.DEException;

import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
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
    Collection<DatasourceConfiguration> datasourceTypes() throws DEException;

    @PostMapping("/validate")
    DatasourceDTO validate(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @DePermit({"m:read", "#p0+':manage'"})
    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @GetMapping("getTableField/{datasourceId}/{tableName}")
    List<TableField> getTableField(@PathVariable("datasourceId") String datasourceId, @PathVariable("tableName") String tableName) throws DEException;

    @DePermit("m:read")
    @PostMapping("list")
    List<DatasourceDTO> list() throws DEException;

    @DePermit({"m:read", "#p0+':manage'"})
    @PostMapping("getTables/{datasourceId}")
    List<DatasetTableDTO> getTables(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/checkApiDatasource")
    ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws DEException;
}
