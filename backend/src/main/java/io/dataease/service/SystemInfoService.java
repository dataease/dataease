package io.dataease.service;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.IPUtils;
import io.dataease.dto.UserLoginInfoDTO;
import org.springframework.stereotype.Service;


@Service
public class SystemInfoService {

    public UserLoginInfoDTO getUserLoginInfo() {
        return new UserLoginInfoDTO(AuthUtils.getUser(), IPUtils.get());
    }

}
