package io.dataease.commons.utils;

public class DorisTableUtils {

    public static String dorisName(String datasetId){
        return "ds_" + datasetId.replace("-", "_");
    }

    public static String doristmpName(String dorisName){
        return "tmp" + dorisName;
    }

}
