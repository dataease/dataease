package io.dataease.xpack.permissions.user.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.domain.UserGridVO;
import io.dataease.request.BaseGridRequest;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.dao.ext.mapper.UserExtMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserServer implements UserApi {

    @Resource
    private PerUserMapper perUserMapper;

    @Resource
    private UserExtMapper userExtMapper;

    @Override
    public IPage<UserGridVO> pager(int goPage, int pageSize, BaseGridRequest request) {
        Page<UserGridVO> page = new Page(goPage, pageSize);
        QueryWrapper<UserGridVO> wrapper = new QueryWrapper<>();
        String keyword = request.getKeyword();
        wrapper.like(StringUtils.isNotBlank(keyword), "u.name", keyword);
        IPage<UserGridVO> pager = userExtMapper.pager(page, wrapper);
        return pager;
    }
}
