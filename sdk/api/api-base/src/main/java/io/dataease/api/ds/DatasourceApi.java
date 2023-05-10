package io.dataease.api.ds;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.ApiDefinition;
import io.dataease.api.ds.vo.DatasourceConfiguration;
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

@DeApiPath(value = "/datasource", rt = "datasource")
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
    Collection<DatasourceConfiguration> datasourceTypes() throws Exception;

    @PostMapping("/validate")
    DatasourceDTO validate(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @DePermit({"m:read", "#p0+':manage'"})
    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") String datasourceId) throws Exception;

    @DePermit("m:read")
    @PostMapping("list")
    List<DatasourceDTO> list() throws Exception;

    @DePermit({"m:read", "#p0+':manage'"})
    @PostMapping("getTables/{datasourceId}")
    List<DatasetTableDTO> getTables(@PathVariable("datasourceId") String datasourceId) throws Exception;

    @PostMapping("/checkApiDatasource")
    ApiDefinition checkApiDatasource(@RequestBody Map<String, String> data) throws Exception;
}
