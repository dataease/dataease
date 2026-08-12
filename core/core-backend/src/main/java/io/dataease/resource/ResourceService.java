package io.dataease.resource;

import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.system.manage.CorePermissionManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Author Junjun
 */
@Component
public class ResourceService {
    @Resource
    private CorePermissionManage corePermissionManage;

    public boolean checkPermission(Long id, String type) {
        BusiPerCheckDTO dto = new BusiPerCheckDTO();
        dto.setId(id);
        dto.setBusiEnum(getResourceTypeEnum(type));
        dto.setAuthEnum(AuthEnum.READ);
        boolean b;
        try {
            b = corePermissionManage.checkAuth(dto);
        } catch (Exception e) {
            b = false;
        }
        return b;
    }

    private BusiResourceEnum getResourceTypeEnum(String type) {
        String t = type.toLowerCase();
        return switch (t) {
            case "datasource" -> BusiResourceEnum.DATASOURCE;
            case "dataset" -> BusiResourceEnum.DATASET;
            case "dashboard" -> BusiResourceEnum.PANEL;
            case "datav" -> BusiResourceEnum.SCREEN;
            default -> null;
        };
    }
}
