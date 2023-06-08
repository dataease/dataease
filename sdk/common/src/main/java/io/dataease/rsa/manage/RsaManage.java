package io.dataease.rsa.manage;

import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.dao.mapper.CoreRsaMapper;
import io.dataease.model.RSAModel;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.RsaUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RsaManage {

    @Resource
    private CoreRsaMapper coreRsaMapper;


    public void check() {
        RsaManage proxy = proxy();
        if (ObjectUtils.isEmpty(proxy.query())) {
            proxy.save();
        }
    }

    @CacheEvict(value = "rsa", key = "'-de-'")
    public void save() {
        RSAModel model = RsaUtils.generate();
        CoreRsa coreRsa = new CoreRsa();
        coreRsa.setId(1);
        coreRsa.setCreateTime(System.currentTimeMillis());
        coreRsa.setPrivateKey(model.getPrivateKey());
        coreRsa.setPublicKey(model.getPublicKey());
        coreRsa.setAesKey(model.getAesKey());
        coreRsaMapper.insert(coreRsa);
    }

    @Cacheable(value = "rsa", key = "'-de-'", unless = "#result == null")
    public CoreRsa query() {
        CoreRsa coreRsa = coreRsaMapper.selectById(1);
        return coreRsa;
    }

    private RsaManage proxy() {
        RsaManage rsaManage = CommonBeanFactory.getBean(RsaManage.class);
        return rsaManage;
    }
}
