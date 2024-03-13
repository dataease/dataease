package io.dataease.iam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.dataease.iam.pojo.dto.IamMsg;
import io.dataease.iam.pojo.entity.OrganizationEntity;

/**
 * Description: OrganizationService
 */
public interface OrganizationService  extends IService<OrganizationEntity>{

    /**
     * 平台通过API给业务系统添加一个组织机构。
     *
     * @param organizationEntity OrganizationEntity
     * @return IamMsg
     */
    IamMsg addOrganization(OrganizationEntity organizationEntity);

    /**
     * 平台给SP修改或移动组织机构接口。
     *
     * @param organizationEntity OrganizationEntity
     * @return IamMsg
     */
    IamMsg updateOrganization(OrganizationEntity organizationEntity);

    /**
     * 平台给SP删除组织机构接口。
     *
     * @param organizationUuid 组织id
     * @return IamMsg
     */
    IamMsg deleteOrganization(String organizationUuid);

}
