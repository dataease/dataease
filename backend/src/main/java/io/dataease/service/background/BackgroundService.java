package io.dataease.service.background;

import io.dataease.plugins.common.base.domain.SysBackgroundImage;
import io.dataease.plugins.common.base.mapper.SysBackgroundImageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2022/2/22
 * Description:
 */
@Service
public class BackgroundService {

    @Resource
    private SysBackgroundImageMapper sysBackgroundImageMapper;

    public Map<String,List<SysBackgroundImage>> findAll(){
        List<SysBackgroundImage> result = sysBackgroundImageMapper.selectByExampleWithBLOBs(null);
        return result.stream().collect(Collectors.groupingBy(SysBackgroundImage::getClassification));
    }

}
