package io.dataease.xpack.permissions.org.server;

import io.dataease.api.permissions.org.api.OrgApi;
import io.dataease.api.permissions.org.dto.OrgCreator;
import io.dataease.api.permissions.org.dto.OrgEditor;
import io.dataease.api.permissions.org.vo.OrgPageVO;
import io.dataease.model.KeywordRequest;
import io.dataease.xpack.permissions.org.dao.auto.entity.PerOrg;
import io.dataease.xpack.permissions.org.manage.OrgPageManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgServer implements OrgApi {

    @Resource
    private OrgPageManage orgPageManage;

    @Override
    public List<OrgPageVO> pageTree(KeywordRequest request) {
        List<PerOrg> orgList = orgPageManage.query(request.getKeyword());
        return orgPageManage.buildTree(orgList);
    }

    @Override
    public void create(OrgCreator creator) {
        orgPageManage.save(creator.getName());
    }

    @Override
    public void edit(OrgEditor editor) {
        orgPageManage.edit(editor.getId(), editor.getName());
    }

    @Override
    public void delete(Long id) {
        orgPageManage.delete(id);
    }
}
