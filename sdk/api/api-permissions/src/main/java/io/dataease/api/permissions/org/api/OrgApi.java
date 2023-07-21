package io.dataease.api.permissions.org.api;

import io.dataease.api.permissions.org.dto.OrgCreator;
import io.dataease.api.permissions.org.dto.OrgEditor;
import io.dataease.api.permissions.org.vo.MountedVO;
import io.dataease.api.permissions.org.vo.OrgPageVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.KeywordRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.ORG;
import static io.dataease.constant.AuthResourceEnum.ROLE;

@DeApiPath(value = "/org", rt = ORG)
public interface OrgApi {

    @PostMapping("/page/tree")
    @DePermit("m:read")
    List<OrgPageVO> pageTree(@RequestBody KeywordRequest request);

    @DePermit({"m:read"})
    @PostMapping("/page/create")
    void create(@RequestBody OrgCreator creator);

    @DePermit({"m:read", "#p0.id+':manage'"})
    @PostMapping("/page/edit")
    void edit(@RequestBody OrgEditor editor);

    @PostMapping("/page/delete/{id}")
    @DePermit({"m:read", "#p0+':manage'"})
    void delete(@PathVariable("id") Long id);

    @PostMapping("/mounted")
    List<MountedVO> mounted(@RequestBody KeywordRequest request);

    @GetMapping("/resourceExist/{oid}")
    boolean resourceExist(@PathVariable("oid") Long oid);
}
