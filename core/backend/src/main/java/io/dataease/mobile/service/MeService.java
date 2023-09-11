package io.dataease.mobile.service;

import io.dataease.ext.MobileMeMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.mobile.dto.MeItemDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MeService {

    @Resource
    private MobileMeMapper mobileMeMapper;

    public MeItemDTO personInfo() {
        return mobileMeMapper.query(AuthUtils.getUser().getUserId());
    }
}
