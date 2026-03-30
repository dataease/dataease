package io.dataease.share.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.exception.DEException;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareMapper;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("shareSecretManage")
public class ShareSecretManage {


    @Getter
    @Value("${dataease.default-link-pwd:link-pwd-fit2cloud}")
    private String defaultPwd;


    @Resource
    private XpackShareMapper xpackShareMapper;

    public String getSecret(Long resourceId, Long uid) {
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator", uid);
        queryWrapper.eq("resource_id", resourceId);
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(xpackShare)) DEException.throwException("Share resource do not exist");
        String sharePwd = xpackShare.getPwd();
        return StringUtils.isNotBlank(sharePwd) ? sharePwd : defaultPwd;
    }
}
