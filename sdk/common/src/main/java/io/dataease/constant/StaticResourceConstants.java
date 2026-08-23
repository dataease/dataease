package io.dataease.constant;

import io.dataease.utils.ConfigUtils;

import java.io.File;

import static io.dataease.utils.StaticResourceUtils.ensureSuffix;


public class StaticResourceConstants {

    public static final String FILE_PROTOCOL = "file:";

    public static final String FILE_SEPARATOR = File.separator;

    public static final String USER_HOME = getHomeData();

    public static String WORK_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "static-resource" + FILE_SEPARATOR;

    // 宿主机持久化目录仅保存用户修改后的内置地图覆盖文件
    public static String MAP_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "map";
    // 镜像内置地图只作为只读原始文件和静态资源回退来源
    public static String MAP_ORIGIN_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "map-origin";
    public static String CUSTOM_MAP_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "geo";
    public static String APPEARANCE_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "appearance";
    public static String REPORT_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "report";
    public static String PLUGIN_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "plugin";
    public static String I18N_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "i18n/front";

    public static String MAP_URL = "/map";
    public static String GEO_URL = "/geo";
    public static String I18N_URL = "/i18n";

    /**
     * Upload prefix.
     */
    public final static String UPLOAD_URL_PREFIX = "static-resource";

    /**
     * url separator.
     */
    public static final String URL_SEPARATOR = "/";

    /**
     * 所有运行模式都优先使用外部 dataease.path.data 配置。
     * 迁移后的 V3 可以将持久化文件保存在独立目录，而不必写入固定的 /opt/dataease3.0/data；
     * 未配置时仍回退到原默认路径，保持现有部署兼容性。
     */
    public static String getHomeData() {
        return ConfigUtils.getConfig("dataease.path.data", "/opt/dataease3.0/data");
    }
}
