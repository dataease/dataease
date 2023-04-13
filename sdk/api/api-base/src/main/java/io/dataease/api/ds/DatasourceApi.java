package io.dataease.api.ds;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;

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
    Collection<DatasourceConfiguration> datasourceTypes() throws Exception;

    @PostMapping("/validate")
    DatasourceDTO validate(@RequestBody DatasourceDTO dataSourceDTO) throws Exception;

    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") String datasourceId) throws Exception;

    @PostMapping("list")
    List<DatasourceDTO> list();

    @PostMapping("getTables/{datasourceId}")
    List<DatasetTableDTO> getTables(@PathVariable("datasourceId") String datasourceId);
}
