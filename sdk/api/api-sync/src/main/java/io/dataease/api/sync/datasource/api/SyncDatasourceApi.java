package io.dataease.api.sync.datasource.api;

import io.dataease.api.sync.datasource.dto.DBTableDTO;
import io.dataease.api.sync.datasource.dto.DatasourceGridRequest;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.ConfigurationSecurity;
import io.dataease.exception.DEException;
import io.dataease.extensions.sync.model.datasource.DatasourceDTO;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.result.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    @ConfigurationSecurity(maskedResponse = {"configuration"})
    PageResult<DatasourceRequest> sourcePager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DatasourceGridRequest request);

    @PostMapping("/target/pager/{goPage}/{pageSize}")
    @ConfigurationSecurity(maskedResponse = {"configuration"})
    PageResult<DatasourceRequest> targetPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DatasourceGridRequest request);

    @PostMapping("/save")
    @ConfigurationSecurity(encryptedRequest = true)
    void save(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @PostMapping("/update")
    @ConfigurationSecurity(encryptedRequest = true)
    Map<String, Object> update(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @PostMapping("/delete/{datasourceId}")
    void delete(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @GetMapping("/types")
    Object datasourceTypes() throws DEException;

    @PostMapping("/validate")
    @ConfigurationSecurity(encryptedRequest = true, maskedResponse = {"configuration"})
    String validate(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @PostMapping("/getSchema")
    @ConfigurationSecurity(encryptedRequest = true)
    List<String> getSchema(@RequestBody DatasourceRequest datasourceRequest) throws DEException;

    @GetMapping("/validate/{datasourceId}")
    @ConfigurationSecurity(maskedResponse = {"configuration"})
    DatasourceDTO validate(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/latestUse/{sourceType}")
    List<String> latestUse(@PathVariable("sourceType") String sourceType);

    @GetMapping("/get/{datasourceId}")
    @ConfigurationSecurity(maskedResponse = {"configuration"})
    DatasourceDTO get(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/batchDel")
    void batchDel(@RequestBody List<String> ids) throws DEException;

    @PostMapping("/fields")
    @ConfigurationSecurity(encryptedRequest = true)
    Map<String, Object> getFields(@RequestBody DatasourceRequest getDsRequest) throws DEException;

    @GetMapping("/list/{type}")
    @ConfigurationSecurity(maskedResponse = {"configuration"})
    List<DatasourceDTO> listByType(
            @PathVariable("type") String type,
            @RequestParam("datasourceRole") Integer datasourceRole) throws DEException;

    @GetMapping("/table/list/{dsId}")
    List<DBTableDTO> getTableList(@PathVariable("dsId") String dsId) throws DEException;

    @GetMapping("/query2Root/{id}")
    String query2Root(@PathVariable("id") Long id);

    @GetMapping("/getLogResourceId/{id}")
    Long getLogResourceId(@PathVariable("id") String id);

}
