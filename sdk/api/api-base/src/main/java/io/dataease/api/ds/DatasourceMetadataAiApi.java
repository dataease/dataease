package io.dataease.api.ds;

import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@DeApiPath(value = "/datasource/metadata", rt = DATASOURCE)
public interface DatasourceMetadataAiApi {
    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/ai/{kind}")
    Task submit(@PathVariable("datasourceId") Long datasourceId, @PathVariable("kind") Kind kind,
                @RequestBody List<String> tables);

    @DePermit({"#p0+':manage'"})
    @GetMapping("/{datasourceId}/ai/{kind}")
    Task status(@PathVariable("datasourceId") Long datasourceId, @PathVariable("kind") Kind kind,
                @RequestParam(value = "taskId", required = false) String taskId);

    enum Kind { RELATION, SEMANTIC }
    enum Status { QUEUED, RUNNING, SUCCESS, PARTIAL, FAILED }
    record Task(String id, Status status, int written, int skipped, int failed) {}
}
