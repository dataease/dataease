package io.dataease.api.sync.datasource.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.sync.datasource.dto.DBTableDTO;
import io.dataease.api.sync.datasource.dto.GetDatasourceRequest;
import io.dataease.api.sync.datasource.dto.SyncDatasourceDTO;
import io.dataease.api.sync.datasource.vo.SyncDatasourceVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.dataease.request.BaseGridRequest;
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

    @DePermit("m:read")
    @PostMapping("/source/pager/{goPage}/{pageSize}")
    IPage<SyncDatasourceVO> sourcePager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @DePermit("m:read")
    @PostMapping("/target/pager/{goPage}/{pageSize}")
    IPage<SyncDatasourceVO> targetPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @PostMapping("/save")
    void save(@RequestBody SyncDatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/update")
    void update(@RequestBody SyncDatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/delete/{datasourceId}")
    void delete(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @GetMapping("/types")
    Object datasourceTypes() throws DEException;

    @PostMapping("/validate")
    String validate(@RequestBody SyncDatasourceDTO dataSourceDTO) throws DEException;

    @PostMapping("/getSchema")
    List<String> getSchema(@RequestBody SyncDatasourceDTO dataSourceDTO) throws DEException;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/validate/{datasourceId}")
    SyncDatasourceDTO validate(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/latestUse/{sourceType}")
    List<String> latestUse(@PathVariable("sourceType") String sourceType);

    @GetMapping("/get/{datasourceId}")
    SyncDatasourceDTO get(@PathVariable("datasourceId") String datasourceId) throws DEException;

    @PostMapping("/batchDel")
    void batchDel(@RequestBody List<String> ids) throws DEException;

    @PostMapping("/fields")
    Map<String, Object> getFields(@RequestBody GetDatasourceRequest getDsRequest) throws DEException ;

    @GetMapping("/list/{type}")
    List<SyncDatasourceDTO> listByType(@PathVariable("type") String type) throws DEException;

    @GetMapping("/table/list/{dsId}")
    List<DBTableDTO> getTableList(@PathVariable("dsId") String dsId) throws DEException;




}
