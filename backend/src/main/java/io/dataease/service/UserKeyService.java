package io.dataease.service;

import io.dataease.base.domain.UserKey;
import io.dataease.base.domain.UserKeyExample;
import io.dataease.base.mapper.UserKeyMapper;
import io.dataease.commons.constants.ApiKeyConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
public class UserKeyService {

    @Resource
    private UserKeyMapper userKeyMapper;

    @Resource
    private UserService userService;

    public List<UserKey> getUserKeysInfo(String userId) {
        UserKeyExample userKeysExample = new UserKeyExample();
        userKeysExample.createCriteria().andUserIdEqualTo(userId);
        userKeysExample.setOrderByClause("create_time");
        return userKeyMapper.selectByExample(userKeysExample);
    }

    public UserKey generateUserKey(String userId) {
        if (userService.getUserDTO(userId) == null) {
            DEException.throwException(Translator.get("user_not_exist") + userId);
        }
        UserKeyExample userKeysExample = new UserKeyExample();
        userKeysExample.createCriteria().andUserIdEqualTo(userId);
        List<UserKey> userKeysList = userKeyMapper.selectByExample(userKeysExample);

        if (!CollectionUtils.isEmpty(userKeysList) && userKeysList.size() >= 5) {
            DEException.throwException(Translator.get("user_apikey_limit"));
        }

        UserKey userKeys = new UserKey();
        userKeys.setId(UUID.randomUUID().toString());
        userKeys.setUserId(userId);
        userKeys.setStatus(ApiKeyConstants.ACTIVE.name());
        userKeys.setAccessKey(RandomStringUtils.randomAlphanumeric(16));
        userKeys.setSecretKey(RandomStringUtils.randomAlphanumeric(16));
        userKeys.setCreateTime(System.currentTimeMillis());
        userKeyMapper.insert(userKeys);
        return userKeyMapper.selectByPrimaryKey(userKeys.getId());
    }

    public void deleteUserKey(String id) {
        userKeyMapper.deleteByPrimaryKey(id);
    }

    public void activeUserKey(String id) {
        UserKey userKeys = new UserKey();
        userKeys.setId(id);
        userKeys.setStatus(ApiKeyConstants.ACTIVE.name());
        userKeyMapper.updateByPrimaryKeySelective(userKeys);
    }

    public void disableUserKey(String id) {
        UserKey userKeys = new UserKey();
        userKeys.setId(id);
        userKeys.setStatus(ApiKeyConstants.DISABLED.name());
        userKeyMapper.updateByPrimaryKeySelective(userKeys);
    }

    public UserKey getUserKey(String accessKey) {
        UserKeyExample userKeyExample = new UserKeyExample();
        userKeyExample.createCriteria().andAccessKeyEqualTo(accessKey).andStatusEqualTo(ApiKeyConstants.ACTIVE.name());
        List<UserKey> userKeysList = userKeyMapper.selectByExample(userKeyExample);
        if (!CollectionUtils.isEmpty(userKeysList)) {
            return userKeysList.get(0);
        }
        return null;
    }
}
