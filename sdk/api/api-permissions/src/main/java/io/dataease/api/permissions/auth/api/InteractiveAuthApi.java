package io.dataease.api.permissions.auth.api;


import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface InteractiveAuthApi {

    @GetMapping("/menuIds")
    List<Long> menuIds();

}
