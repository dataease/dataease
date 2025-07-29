package io.dataease.rsa.manage;

import io.dataease.model.RSAModel;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.dao.mapper.CoreRsaRepository;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.RsaUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import static io.dataease.constant.CacheConstant.CommonCacheConstant.RSA_CACHE;

@Component
public class RsaManage {

    @Resource
    private CoreRsaRepository coreRsaRepository;


    public void check() {
        RsaManage proxy = proxy();
        if (ObjectUtils.isEmpty(proxy.query())) {
            proxy.save();
        }
    }

    @CacheEvict(value = RSA_CACHE, key = "'-de-'")
    public void save() {
        RSAModel model = RsaUtils.generate();
        CoreRsa coreRsa = new CoreRsa();
        coreRsa.setId(1);
        coreRsa.setCreateTime(System.currentTimeMillis());
        coreRsa.setPrivateKey(model.getPrivateKey());
        coreRsa.setPublicKey(model.getPublicKey());
        coreRsa.setAesKey(model.getAesKey());
        coreRsaRepository.saveAndFlush(coreRsa);
    }

    @Cacheable(value = RSA_CACHE, key = "'-de-'", unless = "#result == null")
    public CoreRsa query() {
        return coreRsaRepository.findById(1L).orElse(null);
    }

    private RsaManage proxy() {
        return CommonBeanFactory.getBean(RsaManage.class);
    }
}
