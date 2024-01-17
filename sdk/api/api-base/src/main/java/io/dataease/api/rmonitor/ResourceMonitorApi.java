package io.dataease.api.rmonitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public interface ResourceMonitorApi {

    @GetMapping("/existFree")
    boolean existFree();

    @PostMapping("/delete")
    void delete();

    @PostMapping("/sync")
    void sync();
}
