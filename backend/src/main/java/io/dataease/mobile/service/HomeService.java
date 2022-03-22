package io.dataease.mobile.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.mobile.dto.HomeItemDTO;
import io.dataease.mobile.dto.HomeItemShareDTO;
import io.dataease.base.mapper.ext.HomeMapper;
import io.dataease.mobile.dto.HomeRequest;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeService {

    @Resource
    private HomeMapper homeMapper;

    public Pager<List<HomeItemDTO>> query(HomeRequest request) {
        CurrentUserDto user = AuthUtils.getUser();
        Page<Object> page = PageHelper.startPage(1, 13, true);

        Map<String, Object> param = new HashMap<>();
        param.put("userId", user.getUserId());
        param.put("userId", user.getUserId());
        if (null != request.getLastTime()) {
            param.put("lastTime", request.getLastTime());
        }
        return PageUtils.setPageInfo(page, homeMapper.queryStore(param));
    }

    public Pager<List<HomeItemShareDTO>> queryShares(HomeRequest request) {
        CurrentUserDto user = AuthUtils.getUser();
        Page<Object> page = PageHelper.startPage(1, 13, true);

        Map<String, Object> param = new HashMap<>();
        param.put("userId", user.getUserId());
        Long deptId = user.getDeptId();
        List<Long> roleIds = user.getRoles().stream().map(CurrentRoleDto::getId).collect(Collectors.toList());

        param.put("deptId", deptId);
        param.put("roleIds", roleIds);
        if (null != request.getLastTime()) {
            param.put("lastTime", request.getLastTime());
        }

        return PageUtils.setPageInfo(page, homeMapper.queryShare(param));
    }
}
