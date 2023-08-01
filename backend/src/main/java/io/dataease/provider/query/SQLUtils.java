package io.dataease.provider.query;

/**
 * @Author Junjun
 */
public class SQLUtils {
    public static String transKeyword(String value) {
        if(value == null){
            return null;
        }else{
            return value.replaceAll("'", "\\\\'");
        }
    }
}
