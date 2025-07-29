package io.dataease.copilot.manage;

import io.dataease.api.copilot.dto.TokenDTO;
import io.dataease.copilot.dao.auto.entity.CoreCopilotToken;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotTokenRepository;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Author Junjun
 */
@Component
public class TokenManage {
    @Resource
    private CoreCopilotTokenRepository tokenRepository;

    public TokenDTO getToken(boolean valid) {
        CoreCopilotToken perCopilotToken = tokenRepository.findById(valid ? 2L : 1L).orElse(null) ;
        return transRecord(perCopilotToken);
    }

    public void updateToken(String token, boolean valid) {
        CoreCopilotToken record = new CoreCopilotToken();
        record.setId(valid ? 2L : 1L);
        record.setToken(token);
        record.setUpdateTime(System.currentTimeMillis());
        tokenRepository.saveAndFlush(record);
    }

    private TokenDTO transRecord(CoreCopilotToken perCopilotToken) {
        TokenDTO dto = new TokenDTO();
        BeanUtils.copyBean(dto, perCopilotToken);
        return dto;
    }
}
