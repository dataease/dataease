package io.dataease.auth.handler;

import io.dataease.auth.entity.ASKToken;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.ukey.dto.request.XpackUkeyDto;
import io.dataease.plugins.xpack.ukey.service.UkeyXpackService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class ApiKeyHandler {

    public static final String API_ACCESS_KEY = "accessKey";

    public static final String API_SIGNATURE = "signature";

    public static String random = UUID.randomUUID().toString() + UUID.randomUUID().toString();

    public static Long getUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return getUser(request.getHeader(API_ACCESS_KEY), request.getHeader(API_SIGNATURE));
    }

    public static ASKToken buildToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String accessKey = request.getHeader(API_ACCESS_KEY);
        String signature = request.getHeader(API_SIGNATURE);
        ASKToken askToken = new ASKToken(accessKey, signature);
        return askToken;
    }

    public static Boolean isApiKeyCall(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (StringUtils.isBlank(request.getHeader(API_ACCESS_KEY)) || StringUtils.isBlank(request.getHeader(API_SIGNATURE))) {
            return false;
        }
        return true;
    }

    public static XpackUkeyDto ukey(String accessKey) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        XpackUkeyDto userKey = ukeyXpackService.getUserKey(accessKey);
        return userKey;
    }

    public static Long getUser(String accessKey, String signature) {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(signature)) {
            return null;
        }
        XpackUkeyDto userKey = ukey(accessKey);
        if (userKey == null) {
            throw new RuntimeException("invalid accessKey");
        }
        String signatureDecrypt;
        try {
            signatureDecrypt = CodingUtil.aesDecrypt(signature, userKey.getSecretKey(), accessKey);
        } catch (Throwable t) {
            throw new RuntimeException("invalid signature");
        }
        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(signatureDecrypt), "|");
        if (signatureArray.length < 2) {
            throw new RuntimeException("invalid signature");
        }
        if (!StringUtils.equals(accessKey, signatureArray[0])) {
            throw new RuntimeException("invalid signature");
        }
        long signatureTime = 0l;
        try {
            signatureTime = Long.valueOf(signatureArray[signatureArray.length - 1]).longValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (Math.abs(System.currentTimeMillis() - signatureTime) > 1800000) {
            //签名30分钟超时
            throw new RuntimeException("expired signature");
        }
        return userKey.getUserId();
    }
}
