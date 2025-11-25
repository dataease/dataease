package io.dataease.api.sync.datasource.api;

import io.dataease.api.sync.datasource.dto.DBTableDTO;
import io.dataease.api.sync.datasource.dto.DatasourceGridRequest;
import io.dataease.auth.DeApiPath;
import io.dataease.exception.DEException;
import io.dataease.extensions.sync.model.datasource.DatasourceDTO;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.result.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.SYNC_DATASOURCE;

/**
 * @author fit2cloud
 * @date 2023/11/20 10:14
 **/
@DeApiPath(value = "/sync/datasource", rt = SYNC_DATASOURCE)
public interface SyncDatasourceApi {

    @PostMapping("/source/pager/{goPage}/{pageSize}")
    PageResult<DatasourceRequest> sourcePager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DatasourceGridRequest request);

    @PostMapping("/target/pager/{goPage}/{pageSize}")
    PageResult<DatasourceRequest> targetPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DatasourceGridRequest request);

    @PostMapping("/save")
    void save(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @PostMapping("/update")
    Map<String, Object> update(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @PostMapping("/delete/{datasourceId}")
    void delete(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @GetMapping("/types")
    Object datasourceTypes() throws DEException;

    @PostMapping("/validate")
    String validate(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @PostMapping("/getSchema")
    List<String> getSchema(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @GetMapping("/validate/{datasourceId}")
    DatasourceDTO validate(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/latestUse/{sourceType}")
    List<String> latestUse(@PathVariable("sourceType") String sourceType);

    @GetMapping("/get/{datasourceId}")
    DatasourceDTO get(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/batchDel")
    void batchDel(@RequestBody List<String> ids) throws DEException;

    @PostMapping("/fields")
    Map<String, Object> getFields(@RequestBody DatasourceRequest getDsRequest) throws DEException;

    @GetMapping("/list/{type}")
    List<DatasourceDTO> listByType(@PathVariable("type") String type) throws DEException;

    @GetMapping("/table/list/{dsId}")
    List<DBTableDTO> getTableList(@PathVariable("dsId") String dsId) throws DEException;


}
