package io.dataease.xpack.permissions.user.manage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUserRole;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerRoleMapper;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserRoleMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BatchUserRoleManage extends ServiceImpl<PerUserRoleMapper, PerUserRole> {

    @Resource
    private PerRoleMapper perRoleMapper;
    /**
     * 默认角色ID集合
     * 由系统管理或者application.yml中配置
     */
    private static List<Long> defaultRids = new ArrayList<>();



    @PostConstruct
    public void init() {
        defaultRids.add(3L);
    }

    @Transactional
    public void saveUserRole(List<Long> uids) {
        List<PerUserRole> mappings = defaultRids.stream().flatMap(rid -> {
            PerRole perRole = perRoleMapper.selectById(rid);
            return uids.stream().map(uid -> build(rid, uid, perRole.getOrgId()));
        }).collect(Collectors.toList());
        saveBatch(mappings);
    }

    private PerUserRole build(Long rid, Long uid, Long oid) {
        PerUserRole perUserRole = new PerUserRole();
        perUserRole.setId(IDUtils.snowID());
        perUserRole.setUid(uid);
        perUserRole.setRid(rid);
        perUserRole.setOid(oid);
        perUserRole.setCreateTime(System.currentTimeMillis());
        return perUserRole;
    }


}
