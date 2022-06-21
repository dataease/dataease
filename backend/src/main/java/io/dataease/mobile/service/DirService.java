package io.dataease.mobile.service;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.ext.MobileDirMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.mobile.dto.DirItemDTO;
import io.dataease.mobile.dto.DirRequest;
import io.dataease.mobile.entity.PanelEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirService {

    private static final String[] filterDirNames = { "i18n_panel_list", "i18n_default_panel" };

    @Resource
    private MobileDirMapper mobileDirMapper;

    public List<String> permissions() {
        CurrentUserDto user = AuthUtils.getUser();
        Long userId = user.getUserId();
        Long deptId = user.getDeptId();
        List<String> roles = user.getRoles().stream().map(item -> item.getId().toString()).collect(Collectors.toList());

        List<String> idsWithUser = mobileDirMapper.idsWithUser(userId.toString());
        List<String> idsWithDept = mobileDirMapper.idsWithDept(deptId.toString());
        List<String> idsWithRoles = mobileDirMapper.idsWithRoles(roles);

        List<String> panelIds = new ArrayList<>();
        panelIds.addAll(idsWithUser);
        panelIds.addAll(idsWithDept);
        panelIds.addAll(idsWithRoles);
        return panelIds.stream().distinct().collect(Collectors.toList());
    }

    public List<DirItemDTO> query(DirRequest request) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        List<PanelEntity> panelEntities = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getName())) {
            panelEntities = mobileDirMapper.queryWithName(request.getName(), userId);
        } else {
            panelEntities = mobileDirMapper.query(request.getPid(), userId);
        }
        if (CollectionUtils.isEmpty(panelEntities))
            return null;
        List<String> filterLists = Arrays.asList(filterDirNames);
        List<DirItemDTO> dtos = panelEntities.stream().filter(dto -> !filterLists.contains(dto.getText())).map(data -> {
            DirItemDTO dirItemDTO = new DirItemDTO();
            dirItemDTO.setId(data.getId());
            dirItemDTO.setText(data.getText());
            dirItemDTO.setType(data.getType());
            return dirItemDTO;
        }).collect(Collectors.toList());
        return dtos;
    }

    public DirService proxy() {
        return CommonBeanFactory.getBean(DirService.class);
    }

}
