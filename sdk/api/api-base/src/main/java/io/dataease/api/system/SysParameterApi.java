package io.dataease.api.system;

import io.dataease.api.system.request.OnlineMapEditor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface SysParameterApi {

    @GetMapping("/singleVal/{key}")
    String singleVal(@PathVariable("key") String key);

    @PostMapping("/saveOnlineMap")
    void saveOnlineMap(@RequestBody OnlineMapEditor editor);

    @GetMapping("/queryOnlineMap")
    String queryOnlineMap();

}
