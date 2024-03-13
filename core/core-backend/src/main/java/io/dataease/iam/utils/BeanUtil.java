package io.dataease.iam.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Description: BeanUtil

 */
@Slf4j
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * 将 VO 转换成 DTO
     */
    public static <T> T switchToVO(Object source, Class<T> clazz) {
        return objectToBean(source, clazz);
    }

    /**
     * 将 DTO 转换成 DO
     */
    public static <T> T switchToEntity(Object source, Class<T> clazz) {
        return objectToBean(source, clazz);
    }

    public static <T> T objectToBean(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }

        try {
            String jsonString = JSON.toJSONString(source);
            return JSON.parseObject(jsonString, clazz);
        } catch (RuntimeException e) {
            log.error("", e);
        }
        return null;
    }

    private static Long getLong(String value) {
        Long rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = Long.valueOf(value);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static Integer getInteger(String value) {
        Integer rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = Integer.valueOf(value.split("\\.")[0]);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static Short getShort(String value) {
        Short rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = Short.valueOf(value);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static Double getDouble(String value) {
        Double rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = Double.valueOf(value);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static Float getFloat(String value) {
        Float rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = Float.valueOf(value);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static BigDecimal getBigDecimal(String value) {
        BigDecimal rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = new BigDecimal(value);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static Byte getByte(String value) {
        Byte rs = null;
        try {
            if (StringUtils.isNotEmpty(value)) {
                rs = Byte.valueOf(value);
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
        return rs;
    }

    private static Date getDate(String value, String patter) {
        SimpleDateFormat format = null;
        if (patter != null) {
            format = new SimpleDateFormat(patter);
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        try {
            return format.parse(value);
        } catch (Exception e) {
            log.error("com.yisa.diip.base.utils.BeanUtil.getDate:", e);
        }
        return null;
    }
}
