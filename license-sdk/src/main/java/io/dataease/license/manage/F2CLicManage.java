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
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class F2CLicManage {
    private static final Long LICID = 1L;

    private static final String validatorUtil = "/usr/local/bin/validator";

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

    @Transactional
    public void write(String key, F2CLicResult licResult) {
        CacheUtils.remove(CacheConstant.cacheName, CacheConstant.cacheKey, t -> {
            LicensePO licensePO = new LicensePO(LICID, System.currentTimeMillis(), key, JsonUtil.toJSONString(licResult).toString());
            licenseMapper.deleteById(LICID);
            licenseMapper.insert(licensePO);
        });
        CacheUtils.keyRemove(CacheConstant.LIC_RESULT_CACHE, key);
    }

    @Transactional
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

    private boolean dateMatch(F2CLicResult f2CLicResult) {
        if (f2CLicResult.getStatus() == F2CLicResult.Status.valid) {
            String expired = f2CLicResult.getLicense().getExpired();
            Date date = formatDate(expired);
            if (ObjectUtils.isEmpty(date)) return false;
            return date.getTime() > System.currentTimeMillis();
        }
        return false;
    }

    private Date formatDate(String expired) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(expired);
        } catch (ParseException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return date;
    }


    public F2CLicResult validate(String product, String licenseKey) {
        if (StringUtils.isBlank(licenseKey)) {
            return F2CLicResult.noRecord();
        }
        if (CacheUtils.keyExist(CacheConstant.LIC_RESULT_CACHE, licenseKey)) {
            Object cacheResult = CacheUtils.get(CacheConstant.LIC_RESULT_CACHE, licenseKey);
            if (ObjectUtils.isNotEmpty(cacheResult)) {
                F2CLicResult result = (F2CLicResult) cacheResult;
                if (dateMatch(result)) {
                    return result;
                } else {
                    return F2CLicResult.expired();
                }
            }
            return F2CLicResult.noRecord();
        }
        List<String> command = new ArrayList<String>();
        StringBuilder result = new StringBuilder();
        command.add(validatorUtil);
        command.add(licenseKey);
        try {
            execCommand(result, command);
            LogUtil.info("read lic content is : " + result);
            F2CLicResult f2CLicResult = JsonUtil.parseObject(result.toString(), F2CLicResult.class);

            if (!StringUtils.equals(f2CLicResult.getLicense().getProduct(), product)) {
                f2CLicResult.setStatus(F2CLicResult.Status.invalid);
                f2CLicResult.setLicense(null);
                f2CLicResult.setMessage("The license is unavailable for this product.");
            }
            if (f2CLicResult.getStatus() == F2CLicResult.Status.valid) {
                String expired = f2CLicResult.getLicense().getExpired();
                Date date = formatDate(expired);
                long expiredTime = date.getTime() - System.currentTimeMillis();
                CacheUtils.put(CacheConstant.LIC_RESULT_CACHE, licenseKey, f2CLicResult, expiredTime, TimeUnit.MILLISECONDS);
                return f2CLicResult;
            }
            CacheUtils.put(CacheConstant.LIC_RESULT_CACHE, licenseKey, f2CLicResult);
            return f2CLicResult;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return F2CLicResult.noRecord();
        }
    }

    private static void execCommand(StringBuilder result, List<String> command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        Process process = builder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }
        process.waitFor();
        command.clear();
    }

    @PostConstruct
    public void init() {
    }
}
