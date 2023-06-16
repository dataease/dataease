package io.dataease.xpack.permissions.auth.manage;

import io.dataease.xpack.permissions.org.dao.ext.mapper.OrgResourceMapper;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;


public class OrgResourceManage {


    @Resource
    private OrgResourceMapper orgResourceMapper;


    @Cacheable(value = "org_global_resource", key = "#oid + '1'")
    public List<Long> panelIds(Long oid) {
        return orgResourceMapper.panelIds(oid);
    }

    @Cacheable(value = "org_global_resource", key = "#oid + '2'")
    public List<Long> screenIds(Long oid) {
        return orgResourceMapper.screenIds(oid);
    }

    @Cacheable(value = "org_global_resource", key = "#oid + '3'")
    public List<Long> datasetIds(Long oid) {
        return orgResourceMapper.datasetIds(oid);
    }

    @Cacheable(value = "org_global_resource", key = "#oid + '4'")
    public List<Long> datasourceIds(Long oid) {
        return orgResourceMapper.datasourceIds(oid);
    }

    @Cacheable(value = "org_global_resource", key = "#oid + '5'")
    public List<Long> uids(Long oid) {
        return orgResourceMapper.uids(oid);
    }

    @Cacheable(value = "org_global_resource", key = "#oid + '6'")
    public List<Long> rids(Long oid) {
        return orgResourceMapper.rids(oid);
    }

    @Cacheable(value = "org_global_resource", key = "'7'")
    public List<Long> menuIds() {
        return orgResourceMapper.menuIds();
    }
}
