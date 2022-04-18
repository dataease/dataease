package io.datains.commons.license;

import io.datains.base.domain.License;
import io.datains.base.mapper.LicenseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
class InnerLicenseService {

    @Resource
    private LicenseMapper licenseMapper;

    boolean existLicense(String key) {
        License license = licenseMapper.selectByPrimaryKey(key);
        return license != null;
    }

    License getLicense(String key) {
        License license = licenseMapper.selectByPrimaryKey(key);
        return license;
    }

    void saveLicense(License license) {
        license.setUpdateTime(new Date());
        if (existLicense(license.getId())) {
            licenseMapper.updateByPrimaryKey(license);
        } else {
            licenseMapper.insert(license);
        }
    }


}