package io.dataease.i18n;

import io.dataease.utils.CacheUtils;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import static io.dataease.constant.CacheConstant.UserCacheConstant.USER_COMMUNITY_LANGUAGE;

@Getter
public enum Lang {

    zh_CN("zh-CN"), zh_TW("zh-TW"), en_US("en-US");

    private final String desc;

    Lang(String desc) {
        this.desc = desc;
    }

    public static Lang getLang(String lang) {
        Lang result = getLangWithoutDefault(lang);
        if (result == null) {
            result = zh_CN;
        }
        return result;
    }

    public static Lang getLangWithoutDefault(String lang) {
        if (StringUtils.isBlank(lang)) {
            return null;
        }
        for (Lang lang1 : values()) {
            if (StringUtils.equalsIgnoreCase(lang1.getDesc(), lang)) {
                return lang1;
            }
        }
        if (StringUtils.startsWithIgnoreCase(lang, "zh-CN")) {
            return zh_CN;
        }
        if (StringUtils.startsWithIgnoreCase(lang, "zh-HK") || StringUtils.startsWithIgnoreCase(lang, "zh-TW")) {
            return zh_TW;
        }
        if (StringUtils.startsWithIgnoreCase(lang, "en")) {
            return en_US;
        }
        return null;
    }

    public static boolean isChinese() {
        String lang = null;
        Object langObj = CacheUtils.get(USER_COMMUNITY_LANGUAGE, "de");
        if (ObjectUtils.isNotEmpty(langObj) && StringUtils.isNotBlank(langObj.toString())) {
            lang = langObj.toString();
        }

        if (StringUtils.isBlank(lang)) {
            return true;
        }
        if (StringUtils.startsWithIgnoreCase(lang, "zh")) {
            return true;
        }
        return false;
    }

}
