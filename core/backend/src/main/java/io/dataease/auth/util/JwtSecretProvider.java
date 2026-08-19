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
 * JWT 服务端签名密钥提供者。
 * <p>
 * 启动时从 system_parameter 表读取 jwt.secret；若不存在则生成随机值并持久化，
 * 保证多节点共享同一密钥、重启不丢。JWT 签名/校验使用
 * HMAC256(serverSecret + ":" + userPasswordHash) 的组合密钥，
 * 使伪造 token 需要同时知道服务端密钥与用户密码哈希，同时保留"改密即失效"行为。
 */
@Component
public class JwtSecretProvider implements ApplicationRunner {

    private static volatile String jwtSecret;

    @Resource
    private SystemParameterMapper systemParameterMapper;

    public static String jwtSecret() {
        if (jwtSecret == null) {
            throw new RuntimeException("jwt secret not initialized");
        }
        return jwtSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyEqualTo(ParamConstants.BASIC.JWT_SECRET.getValue());
        List<SystemParameter> list = systemParameterMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list) || StringUtils.isBlank(list.get(0).getParamValue())) {
            SystemParameter parameter = new SystemParameter();
            parameter.setParamKey(ParamConstants.BASIC.JWT_SECRET.getValue());
            parameter.setParamValue(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
            // type 列 NOT NULL 无默认值，必须显式赋值（'text' 原样存取，避免走 password 类型的 AES 加解密，否则重启读取到密文导致 token 全部失效）
            parameter.setType(ParamConstants.Type.TEXT.getValue());
            parameter.setSort(0);
            systemParameterMapper.insert(parameter);
            jwtSecret = parameter.getParamValue();
        } else {
            jwtSecret = list.get(0).getParamValue();
        }
    }
}
