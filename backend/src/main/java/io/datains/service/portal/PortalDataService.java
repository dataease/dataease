package io.datains.service.portal;

import com.github.pagehelper.PageHelper;
import io.datains.base.domain.PortalData;
import io.datains.base.mapper.PortalDataMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据门户 服务实现类
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-06-02
 */
@Service
public class PortalDataService {

    @Resource
    private PortalDataMapper portalDataMapper;

    public Map<String,Object> list(Long userId, Integer pageNum, Integer pageSize){
        Map<String,Object> map = new HashMap<>();
        PageHelper.startPage(pageNum,pageSize);
        List<PortalData> portalData = portalDataMapper.list(userId);
        Integer coutent = portalDataMapper.listCount(userId);
        map.put("total", coutent);
        map.put("portalDataList", portalData);
        return map;
    }


    public void save(PortalData portalData){
        portalDataMapper.save(portalData);
    }


    public void del(Integer id){
        portalDataMapper.del(id);
    }


    public void update(PortalData portalData){
        portalDataMapper.update(portalData);
    }

}
