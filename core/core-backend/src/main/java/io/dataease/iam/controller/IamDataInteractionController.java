package io.dataease.iam.controller;

import io.dataease.iam.pojo.dto.IamMsg;
import io.dataease.iam.pojo.dto.OrganizationDTO;
import io.dataease.iam.pojo.dto.UserDTO;
import io.dataease.iam.pojo.entity.OrganizationEntity;
import io.dataease.iam.pojo.entity.UserEntity;
import io.dataease.iam.service.OrganizationService;
import io.dataease.iam.service.UserService;
import io.dataease.iam.utils.BeanUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description: IamDataInteractionController
 *  IAM数据交互接口
 */
@RestController
@RequestMapping
@Slf4j
public class IamDataInteractionController {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private UserService userService;


    /**
     * 平台通过API给业务系统添加一个组织机构。
     *
     * @param param OrganizationDTO
     * @return IamMsg
     */
//    @TokenIgnore
    @PostMapping(value = "/api/dsg/v1.2/developer/scim/organization")
    public IamMsg addOrganization(@RequestBody @Valid OrganizationDTO param) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>新增部门+pa ram" + param);
        OrganizationEntity organizationEntity = BeanUtil.switchToEntity(param, OrganizationEntity.class);
        if (param.getExtendField() != null && !param.getExtendField().isEmpty()) {
            organizationEntity.setExtendFields(param.getExtendField().toString());
        }

        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>新增部门 +organizationEntity " + organizationEntity);
        return organizationService.addOrganization(organizationEntity);
    }

    /**
     * 平台给SP修改或移动组织机构接口。
     *
     * @param param OrganizationDTO
     * @return IamMsg
     */
//    @TokenIgnore
    @PutMapping(value = "/api/dsg/v1.2/developer/scim/organization", produces = "application/json;charset=utf-8")
    public IamMsg updateOrganization(@RequestBody OrganizationDTO param) {
        OrganizationEntity organizationEntity = BeanUtil.switchToEntity(param, OrganizationEntity.class);
        if (param.getExtendField() != null && !param.getExtendField().isEmpty()) {
            organizationEntity.setExtendFields(param.getExtendField().toString());
        }
        return organizationService.updateOrganization(organizationEntity);
    }

    /**
     * 平台给SP删除组织机构接口。
     *
     * @param organizationUuid 组织id
     * @return IamMsg
     */
//    @TokenIgnore
    @DeleteMapping(value = "/api/dsg/v1.2/developer/scim/organization", produces = "application/json;charset=utf-8")
    public IamMsg deleteOrganization(@NotBlank @RequestParam String organizationUuid) {
        return organizationService.deleteOrganization(organizationUuid);
    }

    /**
     * 同步IAM数据 --新增数据
     *
     * @param param UserDTO
     * @return IamMsg
     */
//    @TokenIgnore
    @PostMapping(value = "/api/dsg/v1.2/developer/scim/account", produces = "application/json;charset=utf-8")
    public IamMsg addUser(@Validated @RequestBody UserDTO param) {
        UserEntity userEntity = BeanUtil.switchToEntity(param, UserEntity.class);
        if (param.getExtendField() != null && !param.getExtendField().isEmpty()) {
            userEntity.setExtendField(param.getExtendField().toString());
        }
        return userService.addUser(userEntity);
    }

    /**
     * 同步IAM数据 --更新数据
     *
     * @param param UserDTO
     * @return IamMsg
     */
//    @TokenIgnore
    @PutMapping(value = "/api/dsg/v1.2/developer/scim/account", produces = "application/json;charset=utf-8")
    public IamMsg updateUser(@Validated @RequestBody UserDTO param) {
        UserEntity userEntity = BeanUtil.switchToEntity(param, UserEntity.class);
        if (param.getExtendField() != null && !param.getExtendField().isEmpty()) {
            userEntity.setExtendField(param.getExtendField().toString());
        }
        return userService.updateUser(userEntity);
    }

    /**
     * 同步IAM数据 --删除数据
     *
     * @param userId 用户id
     * @return IamMsg
     */
//    @TokenIgnore
    @DeleteMapping(value = "/api/dsg/v1.2/developer/scim/account", produces = "application/json;charset=utf-8")
    public IamMsg deleteUser(@RequestParam String userId) {
        return userService.deleteUser(userId);
    }
}
