package io.dataease.api.permissions.embedded.api;

import io.dataease.api.permissions.embedded.dto.EmbeddedCreator;
import io.dataease.api.permissions.embedded.dto.EmbeddedEditor;
import io.dataease.api.permissions.embedded.dto.EmbeddedResetRequest;
import io.dataease.api.permissions.embedded.vo.EmbeddedGridVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface EmbeddedApi {

    @GetMapping("/queryGrid")
    List<EmbeddedGridVO> queryGrid();

    @PostMapping("/create")
    void create(@RequestBody EmbeddedCreator creator);

    @PostMapping("/edit")
    void edit(@RequestBody EmbeddedEditor editor);

    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @PostMapping("/reset")
    void reset(@RequestBody EmbeddedResetRequest request);

    @GetMapping("/domainList")
    List<String> domainList();
}
