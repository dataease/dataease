package io.dataease.license.validator;

import io.dataease.exception.DEException;
import io.dataease.exception.GlobalExceptionHandler;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.config.LicenseValidator;
import io.dataease.license.manage.F2CLicManage;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.result.ResultCode;
import io.dataease.result.ResultMessage;
import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component("1-validator")
public class BaseValidator implements LicenseValidator {

    @Resource
    private F2CLicManage f2CLicManage;

    @Resource
    private GlobalExceptionHandler globalExceptionHandler;

    @Override
    public boolean validate() {
        F2CLicResult f2CLicResult = f2CLicManage.validate();
        LicenseUtil.set(f2CLicResult);
        /*if (f2CLicResult.getStatus() !=  F2CLicResult.Status.valid) {
            ResultMessage message = globalExceptionHandler.deExceptionHandler(new DEException(ResultCode.PERMISSION_NO_ACCESS.code(), f2CLicResult.getMessage()));
            ServletUtils.writeResult(message);
            return false;
        }*/
        return true;
    }
}
