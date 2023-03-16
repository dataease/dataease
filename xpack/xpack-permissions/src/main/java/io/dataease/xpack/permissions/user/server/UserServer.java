package io.dataease.xpack.permissions.user.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.request.BaseGridRequest;
import io.dataease.utils.BeanUtils;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
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

    @Override
    public UserGridVO queryById(Long id) {
        PerUser perUser = perUserMapper.selectById(id);
        UserGridVO userGridVO = new UserGridVO();
        BeanUtils.copyBean(userGridVO, perUser);
        return userGridVO;
    }

    @Override
    public void delete(UserGridVO vo) {
        Long id = vo.getId();
        PerUser perUser = perUserMapper.selectById(id);
        perUser.setEnable(false);
        perUserMapper.updateById(perUser);
    }
}
