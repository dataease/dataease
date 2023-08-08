package io.dataease.api.xpack.component;

import io.dataease.api.xpack.component.vo.XpackMenuVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface XpackComponentApi {

    @GetMapping("/content/{name}")
    String content(@PathVariable("name") String name);

    @GetMapping("/menu")
    List<XpackMenuVO> menu();
}
