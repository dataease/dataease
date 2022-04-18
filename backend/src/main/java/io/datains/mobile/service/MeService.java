package io.datains.mobile.service;

import io.datains.base.mapper.ext.MobileMeMapper;
import io.datains.commons.utils.AuthUtils;
import io.datains.mobile.dto.MeItemDTO;
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
