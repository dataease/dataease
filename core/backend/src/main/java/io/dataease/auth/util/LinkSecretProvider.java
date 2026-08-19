package io.dataease.auth.util;

import io.dataease.commons.constants.ParamConstants;
import io.dataease.plugins.common.base.domain.SystemParameter;
import io.dataease.plugins.common.base.domain.SystemParameterExample;
import io.dataease.plugins.common.base.mapper.SystemParameterMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * 分享链接 JWT 服务端签名密钥提供者。
 * <p>
 * 分享链接在"未启用密码"场景下的 HS256 签名密钥不可硬编码（否则可离线伪造 token），
 * 与登录 JWT 一致：启动时从 system_parameter 表读取 link.secret；若不存在则生成随机值并持久化，
 * 保证多节点共享同一密钥、重启不丢。该密钥仅用于分享链接链路，与登录密钥隔离。
 */
@Component
public class LinkSecretProvider implements ApplicationRunner {

    private static volatile String linkSecret;

    @Resource
    private SystemParameterMapper systemParameterMapper;

    public static String linkSecret() {
        if (linkSecret == null) {
            throw new RuntimeException("link secret not initialized");
        }
        return linkSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyEqualTo(ParamConstants.BASIC.LINK_SECRET.getValue());
        List<SystemParameter> list = systemParameterMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list) || StringUtils.isBlank(list.get(0).getParamValue())) {
            SystemParameter parameter = new SystemParameter();
            parameter.setParamKey(ParamConstants.BASIC.LINK_SECRET.getValue());
            parameter.setParamValue(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
            // type 列 NOT NULL 无默认值，必须显式赋值（'text' 原样存取，避免走 password 类型的 AES 加解密，否则重启读取到密文导致 token 全部失效）
            parameter.setType(ParamConstants.Type.TEXT.getValue());
            parameter.setSort(0);
            systemParameterMapper.insert(parameter);
            linkSecret = parameter.getParamValue();
        } else {
            linkSecret = list.get(0).getParamValue();
        }
    }
}
