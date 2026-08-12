package io.dataease.resource;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("/resource")
public class ResourceApi {
    @Resource
    private ResourceService resourceService;

    @PostMapping("checkPermission/{id}/{type}")
    public boolean checkPermission(@PathVariable("id") Long id, @PathVariable("type") String type) {
        return resourceService.checkPermission(id, type);
    }
}
