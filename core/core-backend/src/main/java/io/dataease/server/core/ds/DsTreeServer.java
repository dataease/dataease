package io.dataease.server.core.ds;

import io.dataease.api.ds.DsTreeApi;
import io.dataease.api.ds.dto.DsTreeDTO;
import io.dataease.api.permissions.AuthApi;
import io.dataease.api.permissions.dto.AuthDTO;
import io.dataease.api.permissions.request.AuthRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询数据源树
 * 以此api演示代码层面无感切换(桌面版、企业版、saas版)3种模式
 * 这就叫做-魔方架构-飞致云独创
 */
@RestController
@RequestMapping("/dsTree")
public class DsTreeServer implements DsTreeApi {

    /**
     * 这个authApi是根据环境注入不同实例的
     * 如果当前环境是(simple单机桌面版)则注入auth的实现类
     * 如果当前环境是(standalone单机企业版)则注入authApi在xpack-auth模块中的实现类
     * 如果当前环境是(distributed分布式Saas版)则注入feignClient的代理类
     */
    @Resource
    private AuthApi authApi;

    @Override
    public List<DsTreeDTO> query(String keyWord) {
        // 获取当前用户Id以及当前组织Id
        /*AuthRequest request = new AuthRequest();
        request.setUserId(1L);
        request.setOrgId(1L);
        request.setResourceTypeId(1);*/
        // 获取根据当前用户及组织查询权限合法的数据源Id集合
        AuthDTO authDTO = authApi.queryByUserId(1L);

        List<DsTreeDTO> result = new ArrayList<>();
        for (long i = 0L; i < 10L; i++) {
            DsTreeDTO item = new DsTreeDTO();
            item.setId(i);
            item.setName("测试数据源【" + i + "】");
            item.setType("mysql");
            result.add(item);
        }
        if (authDTO.isSimple()) return result;
        // 根据查询到到资源ID进行过滤
        return result.stream().filter(item -> authDTO.getResourceId().contains(item.getId())).toList();
    }
}
