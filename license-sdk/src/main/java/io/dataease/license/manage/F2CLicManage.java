package io.dataease.license.manage;

import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.constant.CacheConstant;
import io.dataease.license.dao.mapper.LicenseMapper;
import io.dataease.license.dao.po.LicensePO;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class F2CLicManage {
    private static final Long LICID = 1L;

    private static final String validatorUtil = "/usr/bin/validator";

    private static final String product = "DataEase";
    @Resource
    private LicenseMapper licenseMapper;


    public LicensePO read() {
        Object o = CacheUtils.get(CacheConstant.cacheName, CacheConstant.cacheKey);
        if (ObjectUtils.isNotEmpty(o)) {
            return (LicensePO) o;
        }
        LicensePO po = licenseMapper.selectById(LICID);
        if (ObjectUtils.isEmpty(po)) {
            po = new LicensePO();
        }
        CacheUtils.put(CacheConstant.cacheName, CacheConstant.cacheKey, po);
        return po;
    }

    public void write(String key, F2CLicResult licResult) {
        CacheUtils.remove(CacheConstant.cacheName, CacheConstant.cacheKey, t -> {
            LicensePO licensePO = new LicensePO(LICID, System.currentTimeMillis(), key, JsonUtil.toJSONString(licResult).toString());
            licenseMapper.insert(licensePO);
        });
    }

    public F2CLicResult updateLicense(String product, String licenseKey) {
        F2CLicResult response = validate(product, licenseKey);
        if (response.getStatus() != F2CLicResult.Status.valid) {
            return response;
        }
        write(licenseKey, response);
        return response;
    }

    public F2CLicResult validate() {
        LicensePO po = read();
        return validate(product, po.getLicense());
    }

    public F2CLicResult validate(String product, String licenseKey) {
        if (StringUtils.isBlank(licenseKey)) {
            return F2CLicResult.noRecord();
        }
        List<String> command = new ArrayList<String>();
        StringBuilder result = new StringBuilder();
        command.add(validatorUtil);
        command.add(licenseKey);
        try {
            execCommand(result, command);
            LogUtil.info("read lic content is : " + result.toString());
            F2CLicResult f2CLicResult = JsonUtil.parse(result.toString(), F2CLicResult.class);
            if (f2CLicResult.getStatus() != F2CLicResult.Status.valid) {
                return f2CLicResult;
            }
            if (!StringUtils.equals(f2CLicResult.getLicense().getProduct(), product)) {
                f2CLicResult.setStatus(F2CLicResult.Status.invalid);
                f2CLicResult.setLicense(null);
                f2CLicResult.setMessage("The license is unavailable for this product.");
                return f2CLicResult;
            }
            return f2CLicResult;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return F2CLicResult.noRecord();
        }
    }

    private static int execCommand(StringBuilder result, List<String> command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        Process process = builder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        command.clear();
        return exitCode;
    }

    @PostConstruct
    public void init() {
        System.out.println("123");
    }
}
