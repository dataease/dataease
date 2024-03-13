package io.dataease.iam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.dataease.iam.pojo.dto.IamMsg;
import io.dataease.iam.pojo.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Description: UserBaseService
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 同步IAM数据 --新增数据
     *
     * @param userBaseDTO UserEntity
     * @return IamMsg
     */
    IamMsg addUser(UserEntity userBaseDTO);

    /**
     * 同步IAM数据 --更新数据
     *
     * @param userEntity userEntity
     * @return IamMsg
     */
    IamMsg updateUser(UserEntity userEntity);

    /**
     * 同步IAM数据 --删除数据
     *
     * @param userId 用户id
     * @return IamMsg
     */
    IamMsg deleteUser(String userId);

}