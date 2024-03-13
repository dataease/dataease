package io.dataease.iam.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.iam.mapper.OrganizationMapper;
import io.dataease.iam.mapper.UserMapper;
import io.dataease.iam.pojo.dto.IamMsg;
import io.dataease.iam.pojo.dto.PerUserDTO;
import io.dataease.iam.pojo.entity.OrganizationEntity;
import io.dataease.iam.pojo.entity.UserEntity;
import io.dataease.iam.service.UserService;
import io.dataease.iam.utils.HutoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: UserServiceImpl
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private HutoolUtil hutoolUtil;
    private static final String postAdd = "/de2api/user/create";
    private static final String postEdit = "/de2api/user/edit";

    private static final String postDelete = "/de2api/user/delete";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IamMsg addUser(UserEntity param) {
        log.info("同步数据 用户 新增接口+param " + param);
        UserEntity user = baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserId, param.getUserId())
                .eq(UserEntity::getDeleted, 0));

        //根据组织id查询roleId
        Long roleId = organizationMapper.selectRoleInfoByUuid(param.getOrganizationUuid());

        //存在，更新
        if (user != null) {
            log.info("同步数据 用户  新增里面的更新接口+param" + param);

            baseMapper.update(param, new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getUserId, param.getUserId()));

            // 更新per_user
            JSONObject perUserUpdate = new JSONObject();

            perUserUpdate.set("name", param.getUserName());
            perUserUpdate.set("account", param.getAccount());
            perUserUpdate.set("email", param.getPhoneNumber() + "@youxiang.com");
            List<Long> roleList = new ArrayList<>();
            roleList.add(roleId);
            perUserUpdate.set("roleIds", roleList);
            perUserUpdate.set("enable", param.getLocked() == 0);

            perUserUpdate.set("id", user.getPerUserId());

            JSONObject result = hutoolUtil.postJson(perUserUpdate, postEdit);

            if (result.getInt("code") != 0) {
                throw new RuntimeException("同步到per用户表表异常:" + result.getStr("msg"));
            }
            return IamMsg.success();
        }


        // 新增per_user
        JSONObject perUserAdd = new JSONObject();
        perUserAdd.set("name", param.getUserName());
        perUserAdd.set("account", param.getAccount());
        perUserAdd.set("email", param.getPhoneNumber() + "@youxiang.com");
        List<Long> roleList = new ArrayList<>();
        roleList.add(roleId);
        perUserAdd.set("roleIds", roleList);
        perUserAdd.set("enable", param.getLocked() == 0);

        JSONObject result = hutoolUtil.postJson(perUserAdd, postAdd);

        if (result.getInt("code") != 0) {
            throw new RuntimeException("同步到per用户表表异常:" + result.getStr("msg"));
        }

        //根据account字段获取per_user的id
        Long perUserId = baseMapper.getPerId(param.getAccount());

        //将per_user_id更新回iam_user
        param.setPerUserId(perUserId);
        //不存在，新增
        log.info("同步数据 用户  新增+param " + param);
        baseMapper.insert(param);

        return IamMsg.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IamMsg updateUser(UserEntity userEntity) {
        //更新
        int update = baseMapper.update(userEntity, new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userEntity.getUserId()));
        if (update == 0) {
            return IamMsg.fail("更新iam_user异常");
        }

        // 查
        UserEntity user = baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userEntity.getUserId())
                .eq(UserEntity::getDeleted, 0));

        //根据组织id查询roleId
        Long roleId = organizationMapper.selectRoleInfoByUuid(user.getOrganizationUuid());

        // 更新per_user
        JSONObject perUserUpdate = new JSONObject();

        perUserUpdate.set("name", user.getUserName());
        perUserUpdate.set("account", user.getAccount());
        perUserUpdate.set("email", user.getPhoneNumber() + "@youxiang.com");
        List<Long> roleList = new ArrayList<>();
        roleList.add(roleId);
        perUserUpdate.set("roleIds", roleList);
        perUserUpdate.set("enable", user.getDeleted() == 0);

        perUserUpdate.set("id", user.getPerUserId());

        JSONObject result = hutoolUtil.postJson(perUserUpdate, postEdit);

        if (result.getInt("code") != 0) {
            throw new RuntimeException("同步到per用户表表异常:" + result.getStr("msg"));
        }
        return IamMsg.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IamMsg deleteUser(String userId) {

        // 查
        UserEntity user = baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userId)
                .eq(UserEntity::getDeleted, 0));
        //更新组织api
        JSONObject result = hutoolUtil.postXwForm(user.getPerUserId(), postDelete);

        if (result.getInt("code") == 0) {
            int update = baseMapper.delete(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getUserId, userId));

            if (update == 0) {
                return IamMsg.fail("同步删除iam用户数据异常");
            } else {
                throw new RuntimeException("删除per用户表表异常，userId:" + userId);
            }
        } else {
            return IamMsg.fail("删除pre组织表异常:" + result.getStr("msg"));
        }

    }

}
