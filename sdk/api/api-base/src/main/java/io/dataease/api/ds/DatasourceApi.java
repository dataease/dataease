package io.dataease.api.ds;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.ApiDefinition;
import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.api.ds.vo.TableField;
import io.dataease.auth.DeApiPath;
import io.dataease.exception.DEException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@DeApiPath("/datasource")
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
    DatasourceDTO save(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @PostMapping("/update")
    DatasourceDTO update(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @GetMapping("/types")
    Collection<DatasourceConfiguration> datasourceTypes() throws DEException;

    @PostMapping("/validate")
    DatasourceDTO validate(@RequestBody DatasourceDTO dataSourceDTO) throws DEException;

    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") Long datasourceId) throws DEException;

    @GetMapping("list")
    List<DatasourceDTO> list() throws DEException;

    @GetMapping("getTables/{datasourceId}")
    List<DatasetTableDTO> getTables(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @GetMapping("getTableField/{datasourceId}/{tableName}")
    List<TableField> getTableField(@PathVariable("datasourceId") String datasourceId, @PathVariable("tableName") String tableName) throws DEException;

    @PostMapping("/checkApiDatasource")
    ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws DEException;
}
