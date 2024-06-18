package io.dataease.constant;

import java.io.File;

import static io.dataease.utils.StaticResourceUtils.ensureSuffix;


public class StaticResourceConstants {

    public static final String FILE_PROTOCOL = "file://";

    public static final String FILE_SEPARATOR = File.separator;

    public static final String USER_HOME = "/opt/dataease2.0/data";

    public static String WORK_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "static-resource" + FILE_SEPARATOR;

    public static String MAP_DIR  = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "map";
    public static String CUSTOM_MAP_DIR  = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "geo";
    public static String APPEARANCE_DIR  = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "appearance";
    public static String REPORT_DIR  = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "report";
    public static String PLUGIN_DIR  = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "plugin";

    public static String MAP_URL = "/map";
    public static String GEO_URL = "/geo";

    /**
     * Upload prefix.
     */
    public final static String UPLOAD_URL_PREFIX = "static-resource";

    /**
     * url separator.
     */
    public static final String URL_SEPARATOR = "/";

}
