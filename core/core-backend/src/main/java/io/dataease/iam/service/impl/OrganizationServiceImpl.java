package io.dataease.iam.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.iam.mapper.OrganizationMapper;
import io.dataease.iam.pojo.dto.IamMsg;
import io.dataease.iam.pojo.entity.OrganizationEntity;
import io.dataease.iam.service.OrganizationService;
import io.dataease.iam.utils.HutoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Description: OrganizationServiceImpl
 */
@Service
@Slf4j
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, OrganizationEntity> implements OrganizationService {
    @Autowired
    private HutoolUtil hutoolUtil;
    private static final String postAdd = "/de2api/org/page/create";
    private static final String postEdit = "/de2api/org/page/edit";

    private static final String postDelete = "/de2api/org/page/delete";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IamMsg addOrganization(OrganizationEntity param) {
        // 查有无当前组织
        OrganizationEntity organizationEntity = baseMapper.selectOne(
                new LambdaQueryWrapper<OrganizationEntity>().eq(OrganizationEntity::getDeleted, 0)
                        .eq(OrganizationEntity::getOrganizationUuid, param.getOrganizationUuid()));
        //有，就更新（中间表和api接口）
        if (organizationEntity != null) {
            log.info("同步数据 组织 新增里面 更新接口+param " + param);
            param.setPerOrgId(organizationEntity.getId());
            int update = baseMapper.update(param, new LambdaQueryWrapper<OrganizationEntity>()
                    .eq(OrganizationEntity::getOrganizationUuid, param.getOrganizationUuid()));
            if (update == 0) {
                return IamMsg.fail("同步更新iam组织数据异常");
            }

            //根据id查询pid，name
            OrganizationEntity iamOrganization = baseMapper.selectInfoById(organizationEntity.getId());

            //更新组织api
            return updateOrg(iamOrganization);
        }
        //没有,新增中间表
        log.info("同步数据 组织 新增里面 新增 +param " + param);
        int insert = baseMapper.insert(param);
        if (insert == 0) {
            return IamMsg.fail("同步新增iam组织数据异常");
        }
        param.setPerOrgId(param.getId());
        baseMapper.updateById(param);

        //新增组织api
        //根据id查询pid，name
        OrganizationEntity iamOrganization = baseMapper.selectInfoById(param.getId());

        //新增组织api
        return getIamMsg(iamOrganization, postAdd);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public IamMsg updateOrganization(OrganizationEntity param) {

        int update = baseMapper.update(param, new LambdaQueryWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getOrganizationUuid, param.getOrganizationUuid()));

        if (update == 0) {
            return IamMsg.fail("同步更新iam组织数据异常");
        }

        //根据id查询pid，name
        OrganizationEntity iamOrganization = baseMapper.selectInfoByUuid(param.getOrganizationUuid());
        //更新组织api
        return updateOrg(iamOrganization);
    }

    private IamMsg updateOrg(OrganizationEntity iamOrganization) {
        return getIamMsg(iamOrganization, postEdit);
    }

    private IamMsg getIamMsg(OrganizationEntity iamOrganization, String postAdd) {
        JSONObject pre = new JSONObject();
        pre.set("id", iamOrganization.getPerOrgId());
        pre.set("name", iamOrganization.getOrganization());
        pre.set("pid", iamOrganization.getPerPOrgId());
        JSONObject result = hutoolUtil.postJson(pre, postAdd);

        return result.getInt("code") == 0 ? IamMsg.success() : IamMsg.fail("同步到pre组织表异常:"+result.getStr("msg"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IamMsg deleteOrganization(String organizationUuid) {

        int update = baseMapper.delete(
                new LambdaQueryWrapper<OrganizationEntity>()
                        .eq(OrganizationEntity::getOrganizationUuid, organizationUuid));
        if (update == 0) {
            return IamMsg.fail("同步删除iam组织数据异常");
        }
        //根据id查询pid，name
        OrganizationEntity iamOrganization = baseMapper.selectInfoByUuid(organizationUuid);
        //更新组织api
        JSONObject result = hutoolUtil.postXwForm(iamOrganization.getPerOrgId(), postDelete);

        return result.getInt("code") == 0 ? IamMsg.success() : IamMsg.fail("删除pre组织表异常:"+result.getStr("msg"));
    }

}
