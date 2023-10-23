package io.dataease.substitute.permissions.org;

import io.dataease.api.permissions.org.vo.MountedVO;
import io.dataease.model.KeywordRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnMissingBean(name = "orgServer")
@RestController
@RequestMapping("/org")
public class SubstituleOrgServer {

    @PostMapping("/mounted")
    public List<MountedVO> mounted(KeywordRequest request) {
        MountedVO mountedVO = new MountedVO();
        mountedVO.setId(1L);
        mountedVO.setName("超级管理员");
        mountedVO.setReadOnly(false);
        List<MountedVO> result = new ArrayList<>();
        result.add(mountedVO);
        return result;
    }
}
