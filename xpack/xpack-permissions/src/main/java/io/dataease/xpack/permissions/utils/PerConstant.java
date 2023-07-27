package io.dataease.xpack.permissions.utils;

public class PerConstant {


    public final static Long TOKEN_EXP = 100000L;

    public final static String USER_TOKEN_CACHE = "user_token_cache";

    public static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9\\._-]*$";

    public static final String REGEX_NICKNAME = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    public static final String REGEX_MOBILE = "^((13[0-9])|(15[^4,\\D])|(14[57])|(17[0])|(17[7])|(18[0,0-9]))\\d{8}$";
}
