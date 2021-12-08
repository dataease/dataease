package io.dataease.mobile.service;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.mobile.dto.HomeItemDTO;
import io.dataease.base.mapper.ext.HomeMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeService {

    @Resource
    private HomeMapper homeMapper;

    public List<HomeItemDTO> query(Integer type) {
        List<HomeItemDTO> result = new ArrayList<>();
        CurrentUserDto user = AuthUtils.getUser();
        switch (type){
            case 0:
                result = homeMapper.queryStore(user.getUserId());
                break;
            case 1:
                result = homeMapper.queryHistory();
                break;
            case 2:
                Map<String, Object> param = new HashMap<>();
                Long deptId = user.getDeptId();
                List<Long> roleIds = user.getRoles().stream().map(CurrentRoleDto::getId).collect(Collectors.toList());
                param.put("userId", user.getUserId());
                param.put("deptId", deptId);
                param.put("roleIds", roleIds);
                result = homeMapper.queryShare(param);
                break;
        }
        return result;
    }
}
